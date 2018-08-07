package foundation.icon.iconex.control;

import foundation.icon.iconex.MyConstants;

/**
 * Created by js on 2018. 3. 22..
 */

public class PasswordValidator {

    public static final int OK = 0;
    public static final int NOT_MATCH_PATTERN = 1;
    public static final int HAS_WHITE_SPACE = 2;
    public static final int SERIAL_CHAR = 3;
    public static final int LEAST_8 = 4;

    public static int validatePassword(String pwd) {
        if (pwd.length() < 8) {
            return LEAST_8;
        }

        if (pwd.isEmpty() || pwd.contains(" ")) {
            return HAS_WHITE_SPACE;
        }

        if (!pwd.matches(MyConstants.PATTERN_PASSWORD)) {
            return NOT_MATCH_PATTERN;
        }

        if (!passwordPatternValidate(pwd)) {
            return SERIAL_CHAR;
        }

        return OK;
    }

    private static boolean passwordPatternValidate(String pwd) {
        boolean result = true;

        Character tempChar = null;
        int duplicateCnt = 0;
        int sequenceCnt = 0;
        int revSequenceCnt = 0;
        for (int i = 0; i < pwd.length(); i++) {
            if (i == 0) {
                tempChar = pwd.charAt(i);
            } else {
                if (tempChar.equals(pwd.charAt(i))) {
                    duplicateCnt++;
                    sequenceCnt = 0;
                    revSequenceCnt = 0;
                } else if (((int) tempChar + 1) == (int) pwd.charAt(i)) {
                    duplicateCnt = 0;
                    sequenceCnt++;
                    revSequenceCnt = 0;
                } else if (((int) tempChar - 1) == (int) pwd.charAt(i)) {
                    duplicateCnt = 0;
                    sequenceCnt = 0;
                    revSequenceCnt++;
                } else {
                    duplicateCnt = 0;
                    sequenceCnt = 0;
                    revSequenceCnt = 0;
                }

                tempChar = pwd.charAt(i);
            }

            if (duplicateCnt == 2) {
                result = false;
                return result;
            }
//            else if (sequenceCnt == 2) {
//                result = false;
//                return result;
//            }
//            else if (revSequenceCnt == 2) {
//                result = false;
//                return result;
//            }
        }

        return result;
    }

    public static boolean checkPasswordMatch(String pwd, String checkPwd) {
        if (pwd.isEmpty() || checkPwd.isEmpty()) {
            return false;
        }

        if (!pwd.equals(checkPwd)) {
            return false;
        }

        return true;
    }
}
