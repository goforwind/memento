package com.github.mttkay.memento;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.ComponentName;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MementoTest {

    private static final String TEST_ACTIVITY_CLASS =
            "com.github.mttkay.memento.MementoTest$RetainedActivity";
    private static final String EXPECTED_FRAGMENT_TAG = TEST_ACTIVITY_CLASS + "$Memento";

    @Mock
    private RetainedActivity activityMock;
    @Mock
    private FragmentManager fragmentManagerMock;
    @Mock
    private FragmentTransaction fragmentTransactionMock;

    @Before
    public void setupMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(activityMock.getComponentName()).thenReturn(new ComponentName(
                "com.github.mttkay.memento", TEST_ACTIVITY_CLASS));
        when(activityMock.getClassLoader()).thenReturn(getClass().getClassLoader());
        when(activityMock.getSupportFragmentManager()).thenReturn(fragmentManagerMock);

        when(fragmentTransactionMock.add(any(Fragment.class), anyString())).thenReturn(fragmentTransactionMock);
        when(fragmentManagerMock.beginTransaction()).thenReturn(fragmentTransactionMock);
    }

    @Test
    public void itCreatesAndStoresNewMementoOnFirstLaunch() {
        RetainedActivity hostActivity = Robolectric.buildActivity(RetainedActivity.class).create().get();
        FragmentManager fragmentManager = hostActivity.getSupportFragmentManager();

        assertNull(fragmentManager.findFragmentByTag(EXPECTED_FRAGMENT_TAG));

        Memento.retain(hostActivity);

        assertNotNull(fragmentManager.findFragmentByTag(EXPECTED_FRAGMENT_TAG));
    }

    @Test
    public void itFiresOnLaunchEventOnFirstLaunch() {
        when(fragmentManagerMock.findFragmentByTag(EXPECTED_FRAGMENT_TAG)).thenReturn(null);

        Memento.retain(activityMock);

        verify(activityMock).onLaunch();
    }

    @Test
    public void itRetainsTheHostActivityOnFirstLaunch() {
        when(fragmentManagerMock.findFragmentByTag(EXPECTED_FRAGMENT_TAG)).thenReturn(null);

        Memento.retain(activityMock);

        ArgumentCaptor<RetainedActivity$Memento> memento = ArgumentCaptor.forClass(RetainedActivity$Memento.class);
        verify(fragmentTransactionMock).add(memento.capture(), eq(EXPECTED_FRAGMENT_TAG));

        assertTrue(memento.getValue().retainWasCalled);
    }

    @Test
    public void itRestoresTheHostActivityIfMementoExisted() {
        RetainedActivity$Memento memento = new RetainedActivity$Memento();
        when(fragmentManagerMock.findFragmentByTag(EXPECTED_FRAGMENT_TAG)).thenReturn(memento);

        Memento.retain(activityMock);

        assertTrue(memento.restoreWasCalled);
    }

    static class RetainedActivity extends FragmentActivity implements MementoCallbacks {
        @Override
        public void onLaunch() {
        }
    }

    static class RetainedActivity$Memento extends Fragment implements MementoMethods {

        boolean retainWasCalled;
        boolean restoreWasCalled;

        @Override
        public void retain(FragmentActivity target) {
            retainWasCalled = true;
        }

        @Override
        public void restore(FragmentActivity source) {
            restoreWasCalled = true;
        }
    }
}
