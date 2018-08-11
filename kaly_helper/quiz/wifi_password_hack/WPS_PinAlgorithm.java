import java.util.Scanner;
public class WPS_PinAlgorithm {
    public static void main (String[] args) throws java.lang.Exception
    {
//        String bssid = "48:F8:B3:97:7A:A8";
//	String bssid = "F8:C0:91:25:AC:6C";
	Scanner scanner = new Scanner(System.in);
	System.out.println("Enter mac address (00:00:00:00:00:00)");
	String bssid = scanner.next();
        int[] allPins = calculePin(bssid);
        for (int pin : allPins) {
            System.out.println(String.format("%d", pin));
        }
    }

    public static int[] calculePin(String bssid) {
        int[] ret = new int[3];

        ret[0] = ZhaoChunshengAlgorithm(bssid);
        ret[1] = ArcadyanAlgorithm(bssid);
        ret[2] = ArrisDG860AAlgorithm(bssid);

        return ret;
    }


    private static int fragmentBSSID(String bssid) {
        String[] splitBSSID = bssid.split(":");
        return Integer.valueOf(splitBSSID[0] + splitBSSID[1] + splitBSSID[2], 16).intValue();
    }

    private static int secondFragmentBSSID(String bssid) {
        String[] splitBSSID = bssid.split(":");
        return Integer.valueOf(splitBSSID[3] + splitBSSID[4] + splitBSSID[5], 16).intValue();
    }

    private static int convertToDecimal(String value) {
        return Integer.valueOf(value, 16).intValue();
    }

    private static int ZhaoChunshengAlgorithm(String bssid) {
        int PIN = secondFragmentBSSID(bssid) % 10000000;
        return (PIN * 10) + wpsChecksum(PIN);
    }

    private static int ZhaoChunshengAlgorithmWithoutChecksum(String bssid) {
        return secondFragmentBSSID(bssid) % 10000000;
    }

    private static int ArcadyanAlgorithm(String bssid) {
        int mac = Integer.parseInt(bssid.replaceAll(":", "").substring(8, 12), 16);
        String serial0 = String.format("%05d", new Object[]{Integer.valueOf(mac)});
        int[] sn_int = new int[10];
        sn_int[6] = serial0.charAt(1) & 15;
        sn_int[7] = serial0.charAt(2) & 15;
        sn_int[8] = serial0.charAt(3) & 15;
        sn_int[9] = serial0.charAt(4) & 15;
        String mac_str = bssid.replaceAll(":", "");
        int[] mac_int = new int[mac_str.length()];
        for (byte i = (byte) 0; i < mac_int.length; i = (byte) (i + 1)) {
            mac_int[i] = Integer.parseInt(String.valueOf(mac_str.charAt(i)), 16) & 15;
        }
        int[] hpin = new int[7];
        int k1 = (((sn_int[6] + sn_int[7]) + mac_int[10]) + mac_int[11]) & 15;
        int k2 = (((sn_int[8] + sn_int[9]) + mac_int[8]) + mac_int[9]) & 15;
        hpin[0] = sn_int[9] ^ k1;
        hpin[1] = sn_int[8] ^ k1;
        hpin[2] = mac_int[9] ^ k2;
        hpin[3] = mac_int[10] ^ k2;
        hpin[4] = mac_int[10] ^ sn_int[9];
        hpin[5] = mac_int[11] ^ sn_int[8];
        hpin[6] = sn_int[7] ^ k1;
        int pin = Integer.parseInt(String.format("%1X%1X%1X%1X%1X%1X%1X", new Object[]{Integer.valueOf(hpin[0]), Integer.valueOf(hpin[1]), Integer.valueOf(hpin[2]), Integer.valueOf(hpin[3]), Integer.valueOf(hpin[4]), Integer.valueOf(hpin[5]), Integer.valueOf(hpin[6])}), 16) % 10000000;
        return (pin * 10) + wpsChecksum(pin);
    }

    private static int ArrisDG860AAlgorithm(String strMac) {
        int i;
        long[] fibnum = new long[6];
        long fibsum = 0;
        int counter = 0;
        String[] q = strMac.split(":");
        int[] arrayMacs = new int[q.length];
        int[] tmp = new int[q.length];
        for (int c = 0; c < q.length; c++) {
            arrayMacs[c] = Integer.valueOf(q[c], 16).intValue();
            tmp[c] = Integer.valueOf(q[c], 16).intValue();
        }
        for (i = 0; i < 6; i++) {
            if (tmp[i] > 30) {
                while (tmp[i] > 31) {
                    tmp[i] = tmp[i] - 16;
                    counter++;
                }
            }
            if (counter == 0) {
                if (tmp[i] < 3) {
                    tmp[i] = (((((tmp[0] + tmp[1]) + tmp[2]) + tmp[3]) + tmp[4]) + tmp[5]) - tmp[i];
                    if (tmp[i] > 255) {
                        tmp[i] = tmp[i] & 255;
                    }
                    tmp[i] = (tmp[i] % 28) + 3;
                }
                fibnum[i] = (long) FibGen(tmp[i]);
            } else {
                fibnum[i] = (long) (FibGen(tmp[i]) + FibGen(counter));
            }
            counter = 0;
        }
        for (i = 0; i < 6; i++) {
            fibsum += (fibnum[i] * ((long) FibGen(i + 16))) + ((long) arrayMacs[i]);
        }
        int int_fibsum = (int) (fibsum % 10000000);
        return (int_fibsum * 10) + wpsChecksum(int_fibsum);
    }

    private static String paddingZeros(int pin) {
        String vpin = String.valueOf(pin);
        byte len = (byte) vpin.length();
        if (len < (byte) 8) {
            for (byte i = len; i < (byte) 8; i = (byte) (i + 1)) {
                vpin = "0".concat(vpin);
            }
        }
        return vpin;
    }

    private static int FibGen(int n) {
        if (n == 1 || n == 2 || n == 0) {
            return 1;
        }
        return FibGen(n - 1) + FibGen(n - 2);
    }

    private static int wpsChecksum(int pin) {
        int accum = 0;
        while (pin > 0) {
            accum = (((accum + (pin % 10)*3) | 0) + (((pin / 10)|0) % 10)) | 0;
            pin = (pin/100) | 0;
        }
        accum = (10 - accum % 10) % 10;
        return accum;
    }

    private static int wpsChecksum2(int pin) {
        int accum = 0;
        while (pin > 0) {
            pin /= 10;
            accum = (accum + ((pin % 10) * 3)) + (pin % 10);
            pin /= 10;
        }
        return (10 - (accum % 10)) % 10;
    }
}
