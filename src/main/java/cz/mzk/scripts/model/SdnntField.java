package cz.mzk.scripts.model;

public class SdnntField {

    public static String STATUS = "status";
    public static String LICENCE = "licence";

    public static String DNNT_O = "A";
    public static String DNNT_T = "NZ";
    public static String MAYBE_DNNT = "PA";

    public static String STATUS_A = "dnnto";
    public static String STATUS_NZ = "dnntt";


    public static String dnntLabelByChar(String status) {
        if (status.equals(DNNT_O)) {
            return STATUS_A;
        }
        else if (status.equals(DNNT_T)) {
            return STATUS_NZ;
        }
        else {
            return MAYBE_DNNT;
        }
    }

}
