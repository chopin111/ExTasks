package pl.edu.agh.pp.extasks.tests;

import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TrelloProviderTest extends TestCase{

    @Override
    protected void setUp() throws Exception{
        super.setUp();
        System.out.println("setup");
    }

    @SmallTest
    public void testAdd() {
        Assert.assertEquals(2 + 3, 5);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


}
