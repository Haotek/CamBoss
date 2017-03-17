package general;

import com.tutk.P2PCam264.MyCamera;

/**
 * Created by James Huang on 2015/7/16.
 */
public interface CheckDevListener {

    public static int RESULT_UNKNOWN = - 1;
    public static int RESULT_FAILED = - 2;
    public static int RESULT_WRONG_PWD = - 3;
    public static int RESULT_FAILED_TO_SET = - 4;
    public static int RESULT_FAILED_IN_LAN = - 5;
    public static int RESULT_CANNOT_CHANGE_TO_AP = - 6;
    public static int RESULT_FAILED_TO_CREATE_CHANNEL = - 7;
    public static int RESULT_CANNOT_GET_WIFI_LIST = - 8;
    public static int RESULT_NETWORK_UNREACHABLE = - 9;

    public void getCheckingErr (int result);
    public void getConnected (MyCamera camera);
}
