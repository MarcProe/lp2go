package org.librepilot.lp2go.test;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.librepilot.lp2go.helper.H;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HelperTest {

    private static String LOREMIPSUM = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.";

    @Test
    public void trunc_LoremIpsum_ReturnsSubstring() {
        final String RESULT = "Lorem ipsum dolor sit amet";

        assertThat(H.trunc(LOREMIPSUM, 26), equalTo(RESULT));
    }

    @Test
    public void crc8_LoremIpsum_ReturnsChecksum() {
        final int RESULT = 0x3a;
        final byte[] TEST_BYTE_ARRAY = LOREMIPSUM.getBytes();

        assertThat(H.crc8(TEST_BYTE_ARRAY, 0, TEST_BYTE_ARRAY.length), equalTo(RESULT));
    }

    @Test
    public void calculationByDistance_EltenToKleve_ReturnsDistance() {
        final double RESULT = 8.250642402104924;
        final LatLng ELTEN = new LatLng(51.863454, 6.171409);
        final LatLng KLEVE = new LatLng(51.795390, 6.123602);

        assertThat(H.calculationByDistance(ELTEN, KLEVE), is(RESULT));
    }

}