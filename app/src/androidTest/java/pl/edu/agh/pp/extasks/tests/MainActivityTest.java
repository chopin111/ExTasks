package pl.edu.agh.pp.extasks.tests;

import android.test.ActivityInstrumentationTestCase2;

import pl.edu.agh.pp.extasks.app.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity activity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }

    public void test() {
        assertNotNull(activity);
    }
}
