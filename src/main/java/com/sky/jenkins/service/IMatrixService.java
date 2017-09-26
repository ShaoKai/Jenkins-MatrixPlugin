package com.sky.jenkins.service;

public interface IMatrixService {
    /**
     * @param matrixUrl URL of Matrix API
     * @param matrixUser Account
     * @param matrixPassword Password
     * @return accessToken
     */
    String generateToken(String matrixUrl, String matrixUser, String matrixPassword);

    /**
     * @param matrixUrl URL of Matrix API
     * @param accessToken Access Token
     * @param alias Room Alias
     * @return Room ID
     */
    String getRoomIdByAlias(String matrixUrl, String accessToken, String alias);

    /**
     * @param matrixUrl URL of Matrix API
     * @param accessToken Access Token
     * @param roomId Room ID
     * @return Success or not. Must be invited manually if the room is private.
     */
    boolean joinRoom(String matrixUrl, String accessToken, String roomId);

    /**
     * @param matrixUrl URL of Matrix API
     * @param accessToken Access Token
     * @param roomId Room ID
     * @param message Matrix Message
     * @return Success or not.
     */
    boolean sendMessage(String matrixUrl, String accessToken, String roomId, String message);
}
