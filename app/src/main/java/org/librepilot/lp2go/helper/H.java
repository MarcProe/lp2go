/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.librepilot.lp2go.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;

import com.google.android.gms.maps.model.LatLng;

import org.librepilot.lp2go.VisualLog;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class H {

    public final static String NS = ","; //not seperatoir
    public final static String RS = "\\."; //regex reperator
    public final static String S = "."; //seperator
    //from Utils.Crc
    private final static int CRC_TABLE[] = {
            0x00, 0x07, 0x0e, 0x09, 0x1c, 0x1b, 0x12, 0x15, 0x38, 0x3f, 0x36, 0x31, 0x24, 0x23,
            0x2a, 0x2d,
            0x70, 0x77, 0x7e, 0x79, 0x6c, 0x6b, 0x62, 0x65, 0x48, 0x4f, 0x46, 0x41, 0x54, 0x53,
            0x5a, 0x5d,
            0xe0, 0xe7, 0xee, 0xe9, 0xfc, 0xfb, 0xf2, 0xf5, 0xd8, 0xdf, 0xd6, 0xd1, 0xc4, 0xc3,
            0xca, 0xcd,
            0x90, 0x97, 0x9e, 0x99, 0x8c, 0x8b, 0x82, 0x85, 0xa8, 0xaf, 0xa6, 0xa1, 0xb4, 0xb3,
            0xba, 0xbd,
            0xc7, 0xc0, 0xc9, 0xce, 0xdb, 0xdc, 0xd5, 0xd2, 0xff, 0xf8, 0xf1, 0xf6, 0xe3, 0xe4,
            0xed, 0xea,
            0xb7, 0xb0, 0xb9, 0xbe, 0xab, 0xac, 0xa5, 0xa2, 0x8f, 0x88, 0x81, 0x86, 0x93, 0x94,
            0x9d, 0x9a,
            0x27, 0x20, 0x29, 0x2e, 0x3b, 0x3c, 0x35, 0x32, 0x1f, 0x18, 0x11, 0x16, 0x03, 0x04,
            0x0d, 0x0a,
            0x57, 0x50, 0x59, 0x5e, 0x4b, 0x4c, 0x45, 0x42, 0x6f, 0x68, 0x61, 0x66, 0x73, 0x74,
            0x7d, 0x7a,
            0x89, 0x8e, 0x87, 0x80, 0x95, 0x92, 0x9b, 0x9c, 0xb1, 0xb6, 0xbf, 0xb8, 0xad, 0xaa,
            0xa3, 0xa4,
            0xf9, 0xfe, 0xf7, 0xf0, 0xe5, 0xe2, 0xeb, 0xec, 0xc1, 0xc6, 0xcf, 0xc8, 0xdd, 0xda,
            0xd3, 0xd4,
            0x69, 0x6e, 0x67, 0x60, 0x75, 0x72, 0x7b, 0x7c, 0x51, 0x56, 0x5f, 0x58, 0x4d, 0x4a,
            0x43, 0x44,
            0x19, 0x1e, 0x17, 0x10, 0x05, 0x02, 0x0b, 0x0c, 0x21, 0x26, 0x2f, 0x28, 0x3d, 0x3a,
            0x33, 0x34,
            0x4e, 0x49, 0x40, 0x47, 0x52, 0x55, 0x5c, 0x5b, 0x76, 0x71, 0x78, 0x7f, 0x6a, 0x6d,
            0x64, 0x63,
            0x3e, 0x39, 0x30, 0x37, 0x22, 0x25, 0x2c, 0x2b, 0x06, 0x01, 0x08, 0x0f, 0x1a, 0x1d,
            0x14, 0x13,
            0xae, 0xa9, 0xa0, 0xa7, 0xb2, 0xb5, 0xbc, 0xbb, 0x96, 0x91, 0x98, 0x9f, 0x8a, 0x8d,
            0x84, 0x83,
            0xde, 0xd9, 0xd0, 0xd7, 0xc2, 0xc5, 0xcc, 0xcb, 0xe6, 0xe1, 0xe8, 0xef, 0xfa, 0xfd,
            0xf4, 0xf3
    };
    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private final static int RADIUS = 6371; //the radius of the world.

    public static String intToHex(int i) {
        return bytesToHex(toBytes(i));
    }

    public static int toInt(byte b) {
        return 0xff & b;
    }

    public static int toInt(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public static long toLong(byte[] bytes) {
        long value = 0;
        for (byte aByte : bytes) {
            value = (value << 8) + (aByte & 0xff);
        }
        return value;
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] reverse4bytes(byte[] b) {
        if (b.length != 4) {
            return null;
        }
        byte[] ret = new byte[4];
        ret[0] = b[3];
        ret[1] = b[2];
        ret[2] = b[1];
        ret[3] = b[0];
        return ret;
    }

    public static byte[] reverse8bytes(byte[] b) {
        if (b.length != 8) {
            return null;
        }
        byte[] ret = new byte[8];
        ret[0] = b[7];
        ret[1] = b[6];
        ret[2] = b[5];
        ret[3] = b[4];
        ret[4] = b[3];
        ret[5] = b[2];
        ret[6] = b[1];
        ret[7] = b[0];
        return ret;
    }

    public static byte[] toReversedBytes(short s) {
        byte[] result = new byte[2];

        result[1] = (byte) (s >> 8);
        result[0] = (byte) (s /*>> 0*/);

        return result;
    }

    public static byte[] toBytes(int i) {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }

    public static byte[] toReversedBytes(int i) {
        byte[] result = new byte[4];

        result[3] = (byte) (i >> 24);
        result[2] = (byte) (i >> 16);
        result[1] = (byte) (i >> 8);
        result[1] = (byte) (i /*>> 0*/);

        return result;
    }

    public static byte[] toBytes(long i) {
        byte[] result = new byte[8];

        result[0] = (byte) (i >> 56);
        result[1] = (byte) (i >> 48);
        result[2] = (byte) (i >> 40);
        result[3] = (byte) (i >> 32);
        result[4] = (byte) (i >> 24);
        result[5] = (byte) (i >> 16);
        result[6] = (byte) (i >> 8);
        result[7] = (byte) (i /*>> 0*/);

        return result;
    }

    /*public static String toHex(byte b) {
        byte[] ba = new byte[1];
        ba[0] = b;
        return bytesToPrintHex(ba);
    }*/

    public static String getDateFromMs(String ms) {
        if (ms == null || ms.equals("")) {
            return "";
        }
        long millis = Math.round(H.stringToFloat(ms));
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public static String getDateFromSeconds(String seconds) {
        if (seconds == null || seconds.equals("")) {
            return "";
        }
        long millis = Math.round(H.stringToFloat(seconds) * 1000);
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    //shamelessly stolen from Utils.Crc
    public static int crc8(byte[] b, int start, int len) {
        int crc = 0;
        for (int i = start; i < start + len; i++) {
            crc = (byte) H.CRC_TABLE[((byte) (crc) ^ b[i]) & 0xFF];
        }
        return (crc & 0xFF);
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static double calculationByDistance(LatLng StartP, LatLng EndP) {
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;

        if (lat1 == lat2 && lon1 == lon2) {
            return .0;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return RADIUS * c;
    }

    public static byte[] concatArray(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static String k(String s) {
        /*if(s.length() >3) {
            return s.substring(0,s.length()-3) + "k";
        } else {
            return s;
        }*/
        return s;
    }

    public static byte[] floatToByteArrayRev(float value) {
        return H.reverse4bytes(floatToByteArray(value));
    }

    public static byte[] floatToByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static float stringToFloat(String s) {
        if (s == null || s.equals("")) {
            return .0f;
        }
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            VisualLog.d("stringToFloat", "Fallback to Numberformat");
            try {
                return NumberFormat.getInstance().parse(s).floatValue();
            } catch (ParseException e1) {
                e1.printStackTrace();
                return .0f;
            }
        }
    }

    public static String getLogFilename() {
        //OP-2016-03-02_21-04-56
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return "OP-" + strDate + ".opl";
    }

    public static String trunc(@NonNull final String s, final int l) {
        if (s.length() > l) {
            return s.substring(0, l);
        } else {
            return s;
        }
    }

    public static int dpToPx(int dp, @NonNull Context c) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static Bitmap drawableToBitmap(int imgRes, int dpwidth, int dpheight, @NonNull Context c) {
        return drawableToBitmap(ContextCompat.getDrawable(c, imgRes), dpwidth, dpheight, c);
    }

    public static Bitmap drawableToBitmap(Drawable drawable, int dpwidth, int dpheight, Context c) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = H.dpToPx(dpwidth, c);
        int height = H.dpToPx(dpheight, c);

        VisualLog.d("==>" + width + " " + height);

        //Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
