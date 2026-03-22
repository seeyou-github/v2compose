package io.github.v2compose.util;


import io.github.v2compose.network.NetConstants;



public interface RefererUtils {

    String TINY_REFER = NetConstants.BASE_URL + "/mission/daily";

    public static String topicReferer(String topicId) {
        return NetConstants.BASE_URL + "/t/" + topicId;
    }

    public static String userReferer(String username) {
        return NetConstants.BASE_URL + "/member/" + username;
    }
}
