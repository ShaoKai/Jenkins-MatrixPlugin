package com.sky.jenkins.service;

import com.sky.jenkins.json.LoginParam;
import com.sky.jenkins.json.LoginResp;
import com.sky.jenkins.json.RoomByAliasResp;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.annotation.CheckForNull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MatrixServiceImpl implements IMatrixService {
    private final static String PATH_LOGIN = "/login";
    private final static String PATH_DIRECTORY_ROOM = "/directory/room/";
    private final static String PARAM_ACCESS_TOKEN = "access_token";

    final private static ClientConfig config = new ClientConfig()
            .property(ClientProperties.READ_TIMEOUT, 10000)
            .property(ClientProperties.CONNECT_TIMEOUT, 3000)
            .register(JacksonFeature.class)
            .connectorProvider(new ApacheConnectorProvider());

    private Client client;

    public MatrixServiceImpl() {
        this.client = ClientBuilder.newClient(config);
    }

    @CheckForNull
    @Override
    public String generateToken(String matrixUrl, String matrixUser, String matrixPassword) {

        Response response = this.client.target(matrixUrl)
                .path(PATH_LOGIN)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(
                        LoginParam.builder()
                                .user(matrixUser)
                                .password(matrixPassword)
                                .build()
                ));
        if (response.getStatus() != 200 && response.getStatus() != 201) {
            throw new MatrixException(response);
        }
        return response.readEntity(LoginResp.class).getAccessToken();
    }

    @Override
    public String getRoomIdByAlias(String matrixUrl, String accessToken, String alias) {
        Response response = this.client.target(matrixUrl)
                .path(PATH_DIRECTORY_ROOM + alias)
                .queryParam(PARAM_ACCESS_TOKEN, accessToken)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        if (response.getStatus() != 200 && response.getStatus() != 201) {
            throw new MatrixException(response);
        }
        return response.readEntity(RoomByAliasResp.class).getRoomId();

    }

    @Override
    public boolean joinRoom(String matrixUrl, String accessToken, String roomId) {

        return true;
    }

    @Override
    public boolean sendMessage(String matrixUrl, String accessToken, String roomId, String message) {

        return true;
    }
}
