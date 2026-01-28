#!/bin/bash

# ==============================================================================
# Salesforce ImageOptimization Manager
# Installs/Uninstalls the application and all binary dependencies.
# ==============================================================================

# ------------------------------------------------------------------------------
# Strict Mode & Configuration
# ------------------------------------------------------------------------------
# -e: Exit immediately if a command exits with a non-zero status.
# -u: Treat unset variables as an error when substituting.
# -o pipefail: The return value of a pipeline is the status of the last command 
#              to exit with a non-zero status.
set -euo pipefail

# Constants
APP_NAME="ImageOptimization"
REPO_URL="https://github.com/salesforce/ImageOptimization.git"

# Locations (XDG Standard)
INSTALL_BASE="${XDG_DATA_HOME:-$HOME/.local/share}"
INSTALL_DIR="$INSTALL_BASE/$APP_NAME"
WRAPPER_DIR="${XDG_BIN_HOME:-$HOME/.local/bin}"
WRAPPER_SCRIPT="$WRAPPER_DIR/image-optimizer"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# ------------------------------------------------------------------------------
# Helper Functions
# ------------------------------------------------------------------------------

log_info() { echo -e "${BLUE}[INFO] $1${NC}"; }
log_success() { echo -e "${GREEN}[OK] $1${NC}"; }
log_error() { echo -e "${RED}[ERROR] $1${NC}"; }

# Cleanup trap for temporary files
TEMP_DIR=$(mktemp -d)

cleanup() {
    # Check if TEMP_DIR exists before trying to remove it
    if [[ -d "$TEMP_DIR" ]]; then
        rm -rf "$TEMP_DIR"
    fi
}
trap cleanup EXIT

# ------------------------------------------------------------------------------
# Checks & Validations
# ------------------------------------------------------------------------------

check_dependencies() {
    local missing=()
    for cmd in git curl gcc tar awk grep cut; do
        if ! command -v "$cmd" &> /dev/null; then
            missing+=("$cmd")
        fi
    done

    if [ ${#missing[@]} -gt 0 ]; then
        log_error "Missing required tools: ${missing[*]}"
        exit 1
    fi
}

check_maven_version() {
    # 1. Safety check: ensure maven exists first
    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed."
        exit 1
    fi

    # 2. Get version string (e.g., "3.8.6")
    local mvn_ver=$(mvn -v 2>/dev/null | head -n1 | awk '{print $3}')
    
    # 3. Parse versions using native Bash expansion
    local major_ver="${mvn_ver%%.*}"    # Remove everything after first dot
    local remainder="${mvn_ver#*.}"     # Remove everything before first dot
    local minor_ver="${remainder%%.*}"  # Remove everything after second dot
    
    # 4. Check requirements (Maven 3.3+)
    if [[ "$major_ver" -lt 3 ]] || { [[ "$major_ver" -eq 3 ]] && [[ "$minor_ver" -lt 3 ]]; }; then
        log_error "Maven 3.3+ required. Found $mvn_ver"
        exit 1
    fi
    
    log_success "Maven version $mvn_ver found."
}

check_java_version() {
    if ! command -v java &> /dev/null; then
        return 1
    fi

    local ver=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [[ "$ver" == "1" ]]; then
        ver=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f2);
    fi
    [[ "$ver" =~ ^[0-9]+$ ]] && [[ "$ver" -ge 17 ]]
}

# ------------------------------------------------------------------------------
# Installation Logic
# ------------------------------------------------------------------------------

install_system_packages() {
    log_info "Checking system packages..."
    local DEBIAN_PKGS=(maven imagemagick advancecomp gifsicle optipng pngquant libjpeg-progs webp)
    local RHEL_PKGS=(maven ImageMagick advancecomp gifsicle optipng pngquant libjpeg-turbo-utils libwebp-tools)
    local MACOS_PKGS=(maven imagemagick advancecomp gifsicle optipng pngquant jpeg webp)

    # Add Java if missing or too old
    if ! check_java_version; then
        log_info "Java 17+ not found. Adding to install list..."
        DEBIAN_PKGS+=(openjdk-17-jdk)
        RHEL_PKGS+=(java-17-openjdk-devel)
        MACOS_PKGS+=(openjdk@17)
    fi

    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        if command -v apt-get &> /dev/null; then
            log_info "Detected Debian/Ubuntu..."
            sudo apt-get update && sudo apt-get install -y "${DEBIAN_PKGS[@]}"
        elif command -v dnf &> /dev/null; then
            log_info "Detected Fedora/RHEL..."
            sudo dnf install -y "${RHEL_PKGS[@]}"
        else
            log_error "Unsupported Linux distro. Manual install required."
            exit 1
        fi
        hash -r
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        if ! command -v brew &> /dev/null; then
            log_error "Homebrew required."
            exit 1
        fi
        log_info "Detected macOS. Installing via Homebrew..."

        if ! brew install "${MACOS_PKGS[@]}"; then
            log_error "Homebrew installation failed. Please check the errors above."
            exit 1
        fi
        hash -r

        # Fix: Register Homebrew Java 17 (Keg-Only) if needed
        local brew_java="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk"
        [ -d "/usr/local/opt/openjdk@17/libexec/openjdk.jdk" ] && brew_java="/usr/local/opt/openjdk@17/libexec/openjdk.jdk"
        
        if [ -d "$brew_java" ] && [ ! -d "/Library/Java/JavaVirtualMachines/openjdk-17.jdk" ]; then
            log_info "Linking Homebrew Java 17 to system..."
            sudo ln -sfn "$brew_java" "/Library/Java/JavaVirtualMachines/openjdk-17.jdk" || true
        fi

        if ! command -v jpegtran &> /dev/null; then
            brew link jpeg --force || true;
        fi
    else
        log_error "Unsupported OS: $OSTYPE"
        exit 1
    fi
}

install_app() {
    log_info "=== Starting Installation ==="
    check_dependencies
    install_system_packages

    # Ensure Maven is ready (installed by system packages)
    if ! check_maven_version; then
        log_error "Maven check failed after package installation."
        exit 1
    fi
    # 1. Setup Directories
    local os_subdir="linux"
    [[ "$OSTYPE" == "darwin"* ]] && os_subdir="darwin"
    local bin_dir="$INSTALL_DIR/lib/binary/$os_subdir"
    
    if [ -d "$INSTALL_DIR" ]; then
        rm -rf "$INSTALL_DIR"
    fi
    mkdir -p "$INSTALL_BASE"
    
    # 2. Clone
    log_info "Cloning repository..."
    if ! git clone --depth 1 "$REPO_URL" "$INSTALL_DIR"; then
        log_error "Failed to clone repository from $REPO_URL"
        exit 1
    fi
    cd "$INSTALL_DIR"

    # 3. Build JAR
    log_info "Building Application and bundling dependencies..."
    # Copy dependencies to 'lib' folder to make install self-contained
    if ! mvn clean package dependency:copy-dependencies -DoutputDirectory=target/lib -DskipTests -B -q; then
        log_error "Maven build failed."
        exit 1
    fi

    local jar_file
    jar_file=$(find target -name "ImageOptimization-*.jar" \
        ! -name "original-*" \
        ! -name "*-sources.jar" \
        ! -name "*-javadoc.jar" | head -n 1)

    if [ -z "$jar_file" ]; then log_error "JAR file not found."; exit 1; fi
    
    # Organize final artifact structure
    mkdir -p "$INSTALL_DIR/dist/lib"
    cp "$jar_file" "$INSTALL_DIR/dist/"
    if [ -d "target/lib" ]; then
        cp -r "target/lib/"* "$INSTALL_DIR/dist/lib/"
    fi
    
    # Capture final path
    local final_jar="$INSTALL_DIR/dist/$(basename "$jar_file")"

    # 4. Setup Binaries
    log_info "Configuring binaries in $bin_dir..."
    mkdir -p "$bin_dir"
    local required_bins=(advpng gifsicle optipng pngquant cwebp gif2webp jpegtran)
    for tool in "${required_bins[@]}"; do
        local path=$(command -v "$tool" || true)
        if [ -n "$path" ]; then
            ln -sf "$path" "$bin_dir/$tool"
        else
            log_error "Missing binary: $tool"
            exit 1
        fi
    done

    # 5. Custom Tools (PNGOUT/JFIFREMOVE)
    if [[ "$OSTYPE" == "darwin"* ]]; then
        brew tap jonof/kenutils 2>/dev/null || true
        brew install jonof/kenutils/pngout || true
        [ -x "$(command -v pngout)" ] && ln -sf "$(command -v pngout)" "$bin_dir/pngout"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        log_info "Downloading PNGOUT..."
        local png_url="https://www.jonof.id.au/files/kenutils/pngout-20200115-linux.tar.gz"
        if ! curl -sL --max-time 60 "$png_url" -o "$TEMP_DIR/pngout.tar.gz"; then
            log_error "Failed to download PNGOUT"
            exit 1
        fi
        if ! tar -xzf "$TEMP_DIR/pngout.tar.gz" -C "$TEMP_DIR"; then
            log_error "Failed to extract PNGOUT archive"
            exit 1
        fi
        local arch="x86"; [[ $(uname -m) == "x86_64" ]] && arch="x64"
        if ! find "$TEMP_DIR" -path "*/$arch/pngout" -exec cp {} "$bin_dir/" \; || [ ! -f "$bin_dir/pngout" ]; then
            log_error "Failed to find or copy PNGOUT binary"
            exit 1
        fi
        chmod +x "$bin_dir/pngout"
    fi

    log_info "Compiling JFIFREMOVE..."
    if ! curl -sL --max-time 30 "https://raw.githubusercontent.com/x2q/imgopt/master/jfifremove.c" -o "$TEMP_DIR/jfifremove.c"; then
        log_error "Failed to download jfifremove.c"
        exit 1
    fi
    if ! gcc -O2 -o "$bin_dir/jfifremove" "$TEMP_DIR/jfifremove.c"; then
        log_error "Failed to compile jfifremove"
        exit 1
    fi

    # 6. Create Wrapper
    log_info "Creating wrapper script..."
    mkdir -p "$WRAPPER_DIR"

    cat <<EOF > "$WRAPPER_SCRIPT"
#!/bin/bash
set -euo pipefail

# Auto-detected Java Logic
JAVA_CMD="java"
if [[ "\$OSTYPE" == "darwin"* ]]; then
    if [ -x "/usr/libexec/java_home" ]; then
        JAVA_HOME_17=\$(/usr/libexec/java_home -v 17+ 2>/dev/null | head -n 1)
        [ -n "\$JAVA_HOME_17" ] && JAVA_CMD="\$JAVA_HOME_17/bin/java"
    fi
fi

# Run with wildcard classpath to include dependencies
exec "\$JAVA_CMD" \
    -DbinariesDirectory="$bin_dir" \
    -cp "$final_jar:$INSTALL_DIR/dist/lib/*" \
    com.salesforce.perfeng.uiperf.imageoptimization.Main "\$@"
EOF
    chmod +x "$WRAPPER_SCRIPT"

    # 7. Final Checks
    log_success "Installation Complete!"
    if [[ ":$PATH:" != *":$WRAPPER_DIR:"* ]]; then
        echo -e "${RED}WARNING:${NC} Add $WRAPPER_DIR to your PATH."
    fi
    echo -e "Run using: ${GREEN}image-optimizer${NC}"
}

uninstall_app() {
    log_info "Uninstalling..."
    rm -f "$WRAPPER_SCRIPT"
    rm -rf "$INSTALL_DIR"
    log_success "Uninstalled."
}

if [ "${1:-}" == "uninstall" ]; then
    uninstall_app;
else
    install_app
fi