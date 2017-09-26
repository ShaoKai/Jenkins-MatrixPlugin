package com.sky.jenkins.service;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

public class MatrixServiceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatrixServiceImplTest.class);

    private final static String MATRIX_URL = "http://:8008/_matrix/client/api/v1";
    private final static String HOME_SERVER = "";
    private final static String accessToken = "";


    @Test
    public void generateToken() throws Exception {
        IMatrixService service = new MatrixServiceImpl();
        String accessToken = service.generateToken(
                MATRIX_URL,
                "",
                ""
        );
        assertTrue(accessToken != null);
        LOGGER.info("Access Token :{}", accessToken);

    }

    @Test
    public void getRoomIdByAlias() throws Exception {

        IMatrixService service = new MatrixServiceImpl();
        String roomId = service.getRoomIdByAlias(MATRIX_URL, accessToken, "#Test:" + HOME_SERVER);
        assertTrue(roomId != null);
        LOGGER.info("RoomId :{}", roomId);
    }
}