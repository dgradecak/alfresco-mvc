package com.gradecak.alfresco.mvc.services.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.discussion.DiscussionService;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.gradecak.alfresco.mvc.annotation.AlfrescoTransaction;
import com.gradecak.alfresco.mvc.services.domain.Note;
import com.gradecak.alfresco.mvc.services.domain.Note.NoteType;
import com.gradecak.alfresco.mvc.services.model.IbappModel;

@Service
public class NoteService {

  private static final String COMMENTS = "Comments";

  @Autowired
  private ServiceRegistry serviceRegistry;

  @Autowired
  private DiscussionService discussionService;

  @AlfrescoTransaction
  public Note create(final NodeRef parentRef, final String content) {
    Assert.notNull(parentRef);
    Assert.hasText(content);

    TopicInfo topicInfo = getCommentsContainer(parentRef);
    if (topicInfo == null) {
      topicInfo = createCommentsContainer(parentRef);
    }
    PostInfo post = discussionService.createPost(topicInfo, content);
    return mapComment(post);
  }

  @AlfrescoTransaction
  public Note createSystemNote(final NodeRef parentRef, Map<String, Serializable> systemNote) {
    Assert.notNull(parentRef);
    Assert.notEmpty(systemNote);

    TopicInfo topicInfo = getCommentsContainer(parentRef);
    if (topicInfo == null) {
      topicInfo = createCommentsContainer(parentRef);
    }

    String content = null;
    try {
      content = new ObjectMapper().writeValueAsString(systemNote);
    } catch (JsonProcessingException e) {
      Throwables.propagate(e);
    }
    PostInfo post = discussionService.createPost(topicInfo, content);
    serviceRegistry.getNodeService().addAspect(post.getNodeRef(), IbappModel.ASPECT_SYSTEM, null);

    return mapComment(post);
  }

  @AlfrescoTransaction
  public Note get(final NodeRef noteRef) {
    Assert.notNull(noteRef);

    Pair<TopicInfo, PostInfo> pair = discussionService.getForNodeRef(noteRef);
    PostInfo post = pair.getSecond();
    return mapComment(post);
  }

  @AlfrescoTransaction(readOnly = true)
  public Page<Note> list(final NodeRef parentRef, final Pageable pageable) {
    Assert.notNull(parentRef);

    List<Note> commentList = new ArrayList<Note>();
    TopicInfo topicInfo = getCommentsContainer(parentRef);
    PagingRequest pagingRequest = new PagingRequest(0, Integer.MAX_VALUE);
    if (topicInfo != null) {
      PagingResults<PostInfo> page = discussionService.listPosts(topicInfo, pagingRequest);
      List<PostInfo> postList = page.getPage();
      for (PostInfo postInfo : postList) {
        Note apiComment = mapComment(postInfo);
        commentList.add(apiComment);
      }
      return new PageImpl<>(commentList, pageable, commentList.size());
    }

    return new PageImpl<>(commentList, pageable, 0);
  }

  @AlfrescoTransaction
  public void delete(final NodeRef noteRef) {
    Assert.notNull(noteRef);

    if (ForumModel.TYPE_POST.equals(serviceRegistry.getNodeService().getType(noteRef)) && !serviceRegistry.getNodeService().hasAspect(noteRef, IbappModel.ASPECT_SYSTEM)) {
      Pair<TopicInfo, PostInfo> pair = discussionService.getForNodeRef(noteRef);
      discussionService.deletePost(pair.getSecond());
    } else {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }
  }

  @AlfrescoTransaction
  public Note update(final NodeRef noteRef, final String content) {
    Assert.notNull(noteRef);
    Assert.hasText(content);

    if (ForumModel.TYPE_POST.equals(serviceRegistry.getNodeService().getType(noteRef)) && !serviceRegistry.getNodeService().hasAspect(noteRef, IbappModel.ASPECT_SYSTEM)) {
      Pair<TopicInfo, PostInfo> pair = discussionService.getForNodeRef(noteRef);
      PostInfo post = pair.getSecond();
      post.setContents(content);
      PostInfo updatePost = discussionService.updatePost(post);
      return mapComment(updatePost);
    } else {
      throw new AccessDeniedException("Access not allowed. Please contact the Administrators.");
    }
  }

  private TopicInfo getCommentsContainer(final NodeRef parentRef) {
    Assert.notNull(parentRef);

    List<ChildAssociationRef> childList = serviceRegistry.getNodeService().getChildAssocs(parentRef);
    for (ChildAssociationRef s : childList) {
      if (ForumModel.ASSOC_DISCUSSION.equals(s.getTypeQName())) {
        TopicInfo topic = discussionService.getTopic(s.getChildRef(), COMMENTS);
        return topic;
      }
    }
    return null;
  }

  private TopicInfo createCommentsContainer(final NodeRef parentRef) {
    Assert.notNull(parentRef);

    NodeRef childRef = serviceRegistry.getNodeService().createNode(parentRef, ForumModel.ASSOC_DISCUSSION, ForumModel.ASSOC_DISCUSSION, ForumModel.TYPE_FORUM).getChildRef();
    TopicInfo topic = discussionService.getTopic(childRef, COMMENTS);
    if (topic == null) {
      Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
      properties.put(ContentModel.PROP_NAME, COMMENTS);
      serviceRegistry.getNodeService().createNode(childRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, COMMENTS), ForumModel.TYPE_TOPIC, properties);

      TopicInfo t = getCommentsContainer(parentRef);
      return t;
    }

    return topic;
  }

  private Note mapComment(final PostInfo postInfo) {
    Note note = new Note();
    note.setId(postInfo.getNodeRef());
    note.setCmCreated(postInfo.getCreatedAt());
    note.setCmCreator(postInfo.getCreator());
    note.setCmModified(postInfo.getModifiedAt());
    note.setCmModifier(postInfo.getModifier());
    note.setHtmlContent(postInfo.getContents());

    if (!serviceRegistry.getNodeService().hasAspect(postInfo.getNodeRef(), IbappModel.ASPECT_SYSTEM)) {
      String creator = postInfo.getCreator();
      Boolean canEdit = AuthenticationUtil.getFullyAuthenticatedUser().equals(creator) || serviceRegistry.getAuthorityService().hasAdminAuthority();
      ImmutableMap<String, Boolean> permissions = ImmutableMap.of("edit", canEdit, "delete", canEdit);
      note.setPermissions(permissions);
      note.setType(NoteType.user);
    } else {
      note.setType(NoteType.system);
    }

    return note;
  }
}
