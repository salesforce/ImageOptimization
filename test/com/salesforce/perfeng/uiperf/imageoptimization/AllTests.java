package com.salesforce.perfeng.uiperf.imageoptimization;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.salesforce.perfeng.uiperf.imageoptimization.service.ImageOptimizationServiceTest;
import com.salesforce.perfeng.uiperf.imageoptimization.utils.ImageUtilsTest;

/**
 * Test Suite for the ImageOptimization project
 * 
 * @author eperret (Eric Perret)
 * @since 188.internal
 */
@RunWith(Suite.class)
@SuiteClasses({ ImageOptimizationServiceTest.class, ImageUtilsTest.class})
public class AllTests {
	//Nothing is needed in here.
}