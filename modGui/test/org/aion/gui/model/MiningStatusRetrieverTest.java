package org.aion.gui.model;

import org.aion.api.IAionAPI;
import org.aion.api.IMine;
import org.aion.api.type.ApiMsg;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MiningStatusRetrieverTest {
    @Test
    public void testSuccess() throws Exception {
        KernelConnection kc = mock(KernelConnection.class);
        IAionAPI api = mock(IAionAPI.class);
        when(kc.getApi()).thenReturn(api);
        IMine mine = mock(IMine.class);
        when(api.getMine()).thenReturn(mine);
        ApiMsg msg = mock(ApiMsg.class);
        when(mine.isMining()).thenReturn(msg);
        when(msg.isError()).thenReturn(false);
        when(msg.getObject()).thenReturn(Boolean.valueOf(true));

        MiningStatusRetriever unit = new MiningStatusRetriever(kc);
        assertThat(unit.isMining().get(), is(true));
    }

    @Test
    public void testError() throws Exception {
        KernelConnection kc = mock(KernelConnection.class);
        IAionAPI api = mock(IAionAPI.class);
        when(kc.getApi()).thenReturn(api);
        IMine mine = mock(IMine.class);
        when(api.getMine()).thenReturn(mine);
        ApiMsg msg = mock(ApiMsg.class);
        when(mine.isMining()).thenReturn(msg);
        when(msg.isError()).thenReturn(true);
        when(msg.getErrorCode()).thenReturn(31337);
        when(msg.isError()).thenReturn(true);

        MiningStatusRetriever unit = new MiningStatusRetriever(kc);
        assertThat(unit.isMining().isPresent(), is(false));
    }
}