package com.sky.jenkins;


import com.sky.jenkins.service.IMatrixService;
import com.sky.jenkins.service.MatrixException;
import com.sky.jenkins.service.MatrixServiceImpl;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class MatrixBuilder extends Builder {
    private final String rooms;
    private final String subject;
    private final String message;
    private final String linkUrl;
    private final String linkTitle;


    @DataBoundConstructor
    public MatrixBuilder(String rooms, String subject, String message, String linkUrl, String linkTitle) {
        this.rooms = rooms.trim();
        this.subject = subject.trim();
        this.message = message.trim();
        this.linkUrl = linkUrl.trim();
        this.linkTitle = linkTitle.trim();
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

        Properties props = JenkinsUtils.getPropsFromEnvAndRiotPropFile(build, listener);

        List<String> roomNames = MatrixUtils.splitValueByComma(rooms);
        List<String> allErrors = new ArrayList<>();

        for (String rName : roomNames) {
            if (isBlank(rName)) continue;

            List<String> err = sendMessage(getDescriptor(), props, rName);
            if (err.isEmpty()) {
                listener.getLogger().println(Messages.MatrixBuilder_SendSuccess(rName));
            } else {
                allErrors.addAll(err);
            }
        }

        if (allErrors.size() > 0) {
            StringBuilder buffer = new StringBuilder();
            for (String msg : allErrors) {
                buffer.append(msg).append("\n");
            }
            listener.getLogger().println(buffer.toString());
            return false;
        }
        return true;
    }

    private List<String> sendMessage(MatrixDescriptor descriptor, Properties props, String roomName) {
        List<String> errors = new ArrayList<>();

        final String roomAlias = MatrixUtils.generateRoomName(roomName, getDescriptor().getHomeServer());
        final String accessToken = descriptor.getAccessToken();
        final String matrixUrl = descriptor.getMatrixUrl();

        final String roomId = descriptor.getService().getRoomIdByAlias(matrixUrl, accessToken, roomAlias);
        boolean success = descriptor.getService().joinRoom(matrixUrl, accessToken, roomId);
        if (!success) {
            errors.add(Messages.MatrixBuilder_CannotJoinRoom(roomName));
            errors.add(Messages.MatrixBuilder_PleaseInviteAdminToChatRoom(getDescriptor().getMatrixUser()));
        }

        StringBuilder messageTemplate = new StringBuilder(subject + message);
        if (isNotBlank(linkTitle) && isNotBlank(linkUrl)) {
            messageTemplate.append("<a href='").append(linkUrl).append("'>").append(linkTitle).append("</a>");
        }
        final String message = JenkinsUtils.renderMessage(props, messageTemplate.toString());

        try {
            descriptor.getService().sendMessage(
                    matrixUrl,
                    accessToken,
                    roomId,
                    message
            );
        } catch (MatrixException e) {
            e.printStackTrace();
        }
        return errors;
    }

    @Override
    public MatrixDescriptor getDescriptor() {
        return (MatrixDescriptor) super.getDescriptor();
    }

    @Extension
    public static class MatrixDescriptor extends BuildStepDescriptor<Builder> {
        private static final Logger LOGGER = LoggerFactory.getLogger(MatrixDescriptor.class);

        private final static String MATRIX_URL = "matrixUrl";
        private final static String MATRIX_USER = "matrixUser";
        private final static String MATRIX_PASSWORD = "matrixPassword";
        private final static String AVAILABLE_ROOMS = "availableRooms";
        private final static String HOME_SERVER = "homeServer";

        private String matrixUrl;
        private String matrixUser;
        private String matrixPassword;
        private String availableRooms;
        private String homeServer;

        private String accessToken;
        private String tmpAccessToken;

        private IMatrixService service;

        public MatrixDescriptor() {
            load();
            LOGGER.info("AccessToken :{} ", this.accessToken);
            this.service = new MatrixServiceImpl();
        }

        public String getMatrixUrl() {
            return matrixUrl;
        }

        public String getMatrixUser() {
            return matrixUser;
        }

        public String getMatrixPassword() {
            return matrixPassword;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getAvailableRooms() {
            return availableRooms;
        }

        public String getHomeServer() {
            return homeServer;
        }

        public String getTmpAccessToken() {
            return tmpAccessToken;
        }

        public IMatrixService getService() {
            return service;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindParameters(this);

            this.matrixUrl = formData.getString(MATRIX_URL);
            this.matrixUser = formData.getString(MATRIX_USER);
            this.matrixPassword = formData.getString(MATRIX_PASSWORD);
            this.availableRooms = MatrixUtils.cleanUpAvailableRooms(formData.getString(AVAILABLE_ROOMS));
            this.homeServer = formData.getString(HOME_SERVER);

            //  Store the access token generated by "test connect"
            if (isNotBlank(this.tmpAccessToken)) {
                this.accessToken = this.tmpAccessToken;
            }
            if (isBlank(this.accessToken)) {
                this.accessToken = service.generateToken(matrixUrl, matrixUser, matrixPassword);
            }

            // clean up the temporary value
            this.tmpAccessToken = null;
            save();
            return super.configure(req, formData);
        }

        public FormValidation doTestConnection(
                @QueryParameter(MATRIX_URL) final String matrixUrl,
                @QueryParameter(MATRIX_USER) final String matrixUser,
                @QueryParameter(MATRIX_PASSWORD) final String matrixPassword) throws IOException, ServletException {
            try {
                this.tmpAccessToken = service.generateToken(matrixUrl, matrixUser, matrixPassword);
                return FormValidation.ok(Messages.MatrixBuilder_TestConnectionSuccess());
            } catch (Exception e) {
                e.printStackTrace();
                return FormValidation.error(Messages.MatrixBuilder_TestConnectionFail(e.getMessage()));
            }
        }

        public ListBoxModel doFillRoomsDropdownItems() {
            ListBoxModel items = new ListBoxModel();
            List<String> names = MatrixUtils.splitValueByLineBreak(availableRooms);
            for (String name : names) {
                items.add(name.trim(), name.trim());
            }
            return items;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return FreeStyleProject.class.isAssignableFrom(jobType);
        }

        @Override
        public String getDisplayName() {
            return Messages.MatrixBuilder_DisplayName();
        }


    }

    static class MatrixUtils {
        static String cleanUpAvailableRooms(String newlineSparatedValues) {
            StringBuilder rooms = new StringBuilder();
            for (String r : newlineSparatedValues.split("\n")) {
                String m = r.startsWith("#")
                        ? r.substring(1).split(":")[0]
                        : r.split(":")[0];
                if (isNotBlank(m)) {
                    rooms.append("\n").append(m);
                }
            }
            return rooms.length() > 0 ?
                    rooms.substring(1)
                    : rooms.toString();
        }

        static List<String> splitValueByLineBreak(String values) {
            return Arrays.asList((values.split("\n")));
        }

        static List<String> splitValueByComma(String values) {
            return Arrays.asList((values.split(",")));
        }

        static String generateRoomName(String roomName, String homeServer) {
            return "#" + roomName + ":" + homeServer;
        }
    }

}
