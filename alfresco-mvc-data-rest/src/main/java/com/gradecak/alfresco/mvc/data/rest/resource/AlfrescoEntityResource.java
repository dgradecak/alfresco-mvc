package com.gradecak.alfresco.mvc.data.rest.resource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EmbeddedWrapper;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gradecak.alfresco.mvc.data.mapper.BeanEntityMapper;

public class AlfrescoEntityResource<T extends Persistable<NodeRef>> extends Resource<T> {

  private static final Iterable<EmbeddedWrapper> NO_EMBEDDEDS = new NoLinksResources<EmbeddedWrapper>(Collections.<EmbeddedWrapper> emptyList());

  private final T entity;
  private final Iterable<EmbeddedWrapper> embeddeds;
  private final InputStream inputStream;
  private final BeanEntityMapper<T> mapper;
  private final QName type;

  /**
   * Returns whether the content of the resource is a new entity about to be created. Used to distinguish between
   * creation and updates for incoming requests.
   * 
   * @return
   */
  private final boolean isNew;
  private final boolean nested;

  /**
   * Creates a new {@link PersistentEntityResource} for the given {@link PersistentEntity}, content, embedded
   * {@link Resources}, links and flag whether to render all associations.
   * 
   * @param entity must not be {@literal null}.
   * @param content must not be {@literal null}.
   * @param links must not be {@literal null}.
   * @param embeddeds can be {@literal null}.
   */
  private AlfrescoEntityResource(T entity, QName type, BeanEntityMapper<T> mapper, InputStream inputStream, Iterable<Link> links, Iterable<EmbeddedWrapper> embeddeds, boolean isNew, boolean nested) {

    super(entity, links);

    Assert.notNull(entity, "AlfrescoDomainResource must not be null!");

    this.entity = entity;
    this.embeddeds = embeddeds == null ? NO_EMBEDDEDS : embeddeds;
    this.isNew = isNew;
    this.nested = nested;
    this.mapper = mapper;
    this.inputStream = inputStream;
    this.type = type;
  }

  /**
   * Returns the {@link PersistentEntity} for the underlying instance.
   * 
   * @return
   */
  public T getEntity() {
    return entity;
  }
  
  public QName getType() {
    return type;
  }
  
  public InputStream getInputStream() {
    return inputStream;
  }

  // /**
  // * Returns the {@link PersistentPropertyAccessor} for the underlying content bean.
  // *
  // * @return
  // */
  // public PersistentPropertyAccessor getPropertyAccessor() {
  // return entity.getPropertyAccessor(getContent());
  // }

  /**
   * Returns the resources that are supposed to be rendered in the {@code _embedded} clause.
   * 
   * @return the embeddeds
   */
  public Iterable<EmbeddedWrapper> getEmbeddeds() {
    return embeddeds;
  }

  /**
   * Creates a new {@link Builder} to create {@link PersistentEntityResource}s eventually.
   * 
   * @param content must not be {@literal null}.
   * @param entity must not be {@literal null}.
   * @return
   */
  public static <T extends Persistable<NodeRef>> Builder<T> build(T content, QName type, BeanEntityMapper<T> mapper) {
    return new Builder<T>(content, type, mapper, null);
  }
  
  /**
   * Creates a new {@link Builder} to create {@link PersistentEntityResource}s eventually.
   * 
   * @param content must not be {@literal null}.
   * @param entity must not be {@literal null}.
   * @return
   */
  public static <T extends Persistable<NodeRef>> Builder<T> build(T content, QName type, BeanEntityMapper<T> mapper, InputStream inputStream) {
    return new Builder<T>(content, type, mapper, inputStream);
  }

  /**
   * Builder to create {@link PersistentEntityResource} instances.
   *
   * @author Oliver Gierke
   */
  public static class Builder<T extends Persistable<NodeRef>> {

    private final T content;
    private final QName type;
    private final BeanEntityMapper<T> mapper;
    private final InputStream inputStream;
    private final List<Link> links = new ArrayList<Link>();

    private Iterable<EmbeddedWrapper> embeddeds;    

    /**
     * Creates a new {@link Builder} instance for the given content and {@link PersistentEntity}.
     * 
     * @param content must not be {@literal null}.
     * @param entity must not be {@literal null}.
     */
    private Builder(T content, QName type, BeanEntityMapper<T> mapper, InputStream inputStream) {

      Assert.notNull(content, "Content must not be null!");

      this.content = content;
      this.inputStream = inputStream;
      this.mapper = mapper;
      this.type = type;
    }

    /**
     * Configures the builder to embed the given {@link EmbeddedWrapper} instances. Creates a {@link Resources} instance
     * to make sure the {@link EmbeddedWrapper} handling gets applied to the serialization output ignoring the links.
     * 
     * @param resources can be {@literal null}.
     * @return the builder
     */
    public Builder<T> withEmbedded(Iterable<EmbeddedWrapper> resources) {

      this.embeddeds = resources == null ? null : new NoLinksResources<EmbeddedWrapper>(resources);
      return this;
    }

    /**
     * Adds the given {@link Link} to the {@link PersistentEntityResource}.
     * 
     * @param link must not be {@literal null}.
     * @return the builder
     */
    public Builder<T> withLink(Link link) {

      Assert.notNull(link, "Link must not be null!");

      this.links.add(link);
      return this;
    }

    public Builder<T> withLinks(List<Link> links) {

      Assert.notNull(links, "Links must not be null!");

      this.links.addAll(links);
      return this;
    }

    /**
     * Finally creates the {@link PersistentEntityResource} instance.
     * 
     * @return
     */
    public AlfrescoEntityResource<T> build() {
      return new AlfrescoEntityResource<T>(content, type, mapper, inputStream, links, embeddeds, false, false);
    }

    /**
     * Finally creates the {@link PersistentEntityResource} instance to symbolize the contained entity is about to be
     * created.
     * 
     * @return
     */
    public AlfrescoEntityResource<T> forCreation() {
      return new AlfrescoEntityResource<T>(content, type, mapper, inputStream, links, embeddeds, true, false);
    }

    public AlfrescoEntityResource<T> buildNested() {
      return new AlfrescoEntityResource<T>(content, type, mapper, inputStream, links, embeddeds, false, true);
    }
  }

  private static class NoLinksResources<T> extends Resources<T> {

    public NoLinksResources(Iterable<T> content) {
      super(content);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.hateoas.ResourceSupport#getLinks()
     */
    @Override
    @JsonIgnore
    public List<Link> getLinks() {
      return super.getLinks();
    }
  }
}
