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
package net.proest.lp2go3.c;

public class PID {
    public static final int PID_RATE_ROLL_PROP_DENOM = 100000;
    public static final int PID_RATE_ROLL_PROP_STEP = 10;
    public static final int PID_RATE_ROLL_PROP_MAX = 1000;
    public static final String PID_RATE_ROLL_PROP_DFS = "0.00000";

    public static final int PID_RATE_PITCH_PROP_DENOM = 100000;
    public static final int PID_RATE_PITCH_PROP_STEP = 10;
    public static final int PID_RATE_PITCH_PROP_MAX = 1000;
    public static final String PID_RATE_PITCH_PROP_DFS = "0.00000";

    public static final int PID_RATE_YAW_PROP_DENOM = 100000;
    public static final int PID_RATE_YAW_PROP_STEP = 10;
    public static final int PID_RATE_YAW_PROP_MAX = 1000;
    public static final String PID_RATE_YAW_PROP_DFS = "0.00000";

    public static final int PID_RATE_ROLL_INTE_DENOM = 100000;
    public static final int PID_RATE_ROLL_INTE_STEP = 10;
    public static final int PID_RATE_ROLL_INTE_MAX = 1000;
    public static final String PID_RATE_ROLL_INTE_DFS = "0.00000";

    public static final int PID_RATE_PITCH_INTE_DENOM = 100000;
    public static final int PID_RATE_PITCH_INTE_STEP = 10;
    public static final int PID_RATE_PITCH_INTE_MAX = 1000;
    public static final String PID_RATE_PITCH_INTE_DFS = "0.00000";

    public static final int PID_RATE_YAW_INTE_DENOM = 100000;
    public static final int PID_RATE_YAW_INTE_STEP = 10;
    public static final int PID_RATE_YAW_INTE_MAX = 1000;
    public static final String PID_RATE_YAW_INTE_DFS = "0.00000";

    public static final int PID_RATE_ROLL_DERI_DENOM = 1000000;
    public static final int PID_RATE_ROLL_DERI_STEP = 1;
    public static final int PID_RATE_ROLL_DERI_MAX = 1000;
    public static final String PID_RATE_ROLL_DERI_DFS = "0.000000";

    public static final int PID_RATE_PITCH_DERI_DENOM = 1000000;
    public static final int PID_RATE_PITCH_DERI_STEP = 1;
    public static final int PID_RATE_PITCH_DERI_MAX = 1000;
    public static final String PID_RATE_PITCH_DERI_DFS = "0.000000";

    public static final int PID_RATE_YAW_DERI_DENOM = 1000000;
    public static final int PID_RATE_YAW_DERI_STEP = 1;
    public static final int PID_RATE_YAW_DERI_MAX = 1000;
    public static final String PID_RATE_YAW_DERI_DFS = "0.000000";

    public static final int PID_ROLL_PROP_DENOM = 100000;
    public static final int PID_ROLL_PROP_STEP = 10000;
    public static final int PID_ROLL_PROP_MAX = 500000;
    public static final String PID_ROLL_PROP_DFS = "0.000";

    public static final int PID_PITCH_PROP_DENOM = 100000;
    public static final int PID_PITCH_PROP_STEP = 10000;
    public static final int PID_PITCH_PROP_MAX = 500000;
    public static final String PID_PITCH_PROP_DFS = "0.000";

    public static final int PID_YAW_PROP_DENOM = 100000;
    public static final int PID_YAW_PROP_STEP = 10000;
    public static final int PID_YAW_PROP_MAX = 500000;
    public static final String PID_YAW_PROP_DFS = "0.000";

    public static final int PID_ROLL_INTE_DENOM = 100000;
    public static final int PID_ROLL_INTE_STEP = 10000;
    public static final int PID_ROLL_INTE_MAX = 500000;
    public static final String PID_ROLL_INTE_DFS = "0.000";

    public static final int PID_PITCH_INTE_DENOM = 100000;
    public static final int PID_PITCH_INTE_STEP = 10000;
    public static final int PID_PITCH_INTE_MAX = 500000;
    public static final String PID_PITCH_INTE_DFS = "0.000";

    public static final int PID_YAW_INTE_DENOM = 100000;
    public static final int PID_YAW_INTE_STEP = 10000;
    public static final int PID_YAW_INTE_MAX = 500000;
    public static final String PID_YAW_INTE_DFS = "0.000";

    public static final int PID_VERTICAL_ALTI_PROP_DENOM = 10000;
    public static final int PID_VERTICAL_ALTI_PROP_STEP = 100;
    public static final int PID_VERTICAL_ALTI_PROP_MAX = 10000;
    public static final String PID_VERTICAL_ALTI_PROP_DFS = "0.00";

    public static final int PID_VERTICAL_EXPO_DENOM = 1;
    public static final int PID_VERTICAL_EXPO_STEP = 1;
    public static final int PID_VERTICAL_EXPO_MAX = 255;
    public static final String PID_VERTICAL_EXPO_DFS = "0";

    public static final int PID_VERTICAL_THRUST_R_DENOM = 1;
    public static final int PID_VERTICAL_THRUST_R_STEP = 1;
    public static final int PID_VERTICAL_THRUST_R_MAX = 5;
    public static final String PID_VERTICAL_THRUST_R_DFS = "0";

    public static final int PID_VERTICAL_VELO_BETA_DENOM = 10000;
    public static final int PID_VERTICAL_VELO_BETA_STEP = 100;
    public static final int PID_VERTICAL_VELO_BETA_MAX = 10000;
    public static final String PID_VERTICAL_VELO_BETA_DFS = "0.00";

    public static final int PID_VERTICAL_VELO_DERI_DENOM = 100000;
    public static final int PID_VERTICAL_VELO_DERI_STEP = 10;
    public static final int PID_VERTICAL_VELO_DERI_MAX = 3000;
    public static final String PID_VERTICAL_VELO_DERI_DFS = "0.0000";

    public static final int PID_VERTICAL_VELO_INTE_DENOM = 10000;
    public static final int PID_VERTICAL_VELO_INTE_STEP = 100;
    public static final int PID_VERTICAL_VELO_INTE_MAX = 10000;
    public static final String PID_VERTICAL_VELO_INTE_DFS = "0.00";

    public static final int PID_VERTICAL_VELO_PROP_DENOM = 10000;
    public static final int PID_VERTICAL_VELO_PROP_STEP = 100;
    public static final int PID_VERTICAL_VELO_PROP_MAX = 10000;
    public static final String PID_VERTICAL_VELO_PROP_DFS = "0.00";

}
