package com.gradecak.alfresco.mvc.services.domain;

import org.springframework.hateoas.core.Relation;

import com.gradecak.alfresco.mvc.data.domain.CoreNode;

@Relation(value="user", collectionRelation="users")
public class CmUser extends CoreNode {

  private String cmEmail;
  private String cmLastName;
  private String cmFirstName;
  private String cmUserName;

  public CmUser() {}

  public String getCmEmail() {
    return cmEmail;
  }

  public void setCmEmail(String cmEmail) {
    this.cmEmail = cmEmail;
  }

  public String getCmLastName() {
    return cmLastName;
  }

  public void setCmLastName(String cmLastName) {
    this.cmLastName = cmLastName;
  }

  public String getCmFirstName() {
    return cmFirstName;
  }

  public void setCmFirstName(String cmFirstName) {
    this.cmFirstName = cmFirstName;
  }

  public String getCmUserName() {
    return cmUserName;
  }

  public void setCmUserName(String cmUserName) {
    this.cmUserName = cmUserName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
    result = prime * result + ((cmUserName == null) ? 0 : cmUserName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CmUser other = (CmUser) obj;
    if (getId() == null) {
      if (other.getId() != null)
        return false;
    } else if (!getId().equals(other.getId()))
      return false;
    if (cmUserName == null) {
      if (other.cmUserName != null)
        return false;
    } else if (!cmUserName.equals(other.cmUserName))
      return false;
    return true;
  }
}