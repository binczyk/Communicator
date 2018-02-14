package common;

import java.util.StringTokenizer;

public class Utils {

    public static String getRoomName(StringTokenizer st) {
        StringBuilder roomName = new StringBuilder();
        while (st.hasMoreTokens()) {
            roomName.append(st.nextToken());
            if (roomName.toString().contains("</>")) {
                int indexOfEnd = roomName.indexOf("</>");
                roomName.replace(indexOfEnd, indexOfEnd + 3, "");
                break;
            }
            roomName.append(" ");
        }
        return roomName.toString().trim();
    }

}
