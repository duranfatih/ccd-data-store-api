package uk.gov.hmcts.ccd.domain.model.definition;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;


public class AccessControlList implements Serializable {

    private String accessProfile;
    private boolean create;
    private boolean read;
    private boolean update;
    private boolean delete;

    @JsonGetter("role")
    public String getAccessProfile() {
        return accessProfile;
    }

    @JsonProperty("accessProfile")
    @JsonAlias("role")
    public void setAccessProfile(String accessProfile) {
        this.accessProfile = accessProfile;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    @JsonIgnore
    public AccessControlList duplicate() {
        AccessControlList dup = new AccessControlList();
        dup.setAccessProfile(accessProfile);
        dup.setCreate(create);
        dup.setRead(read);
        dup.setUpdate(update);
        dup.setDelete(delete);
        return dup;
    }

    @Override
    public String toString() {
        return "ACL{"
            + "accessProfile='" + accessProfile + '\''
            + ", crud=" + (isCreate() ? "C" : "") + (isRead() ? "R" : "")
            + (isUpdate() ? "U" : "") + (isDelete() ? "D" : "")
            + '}';
    }
}
