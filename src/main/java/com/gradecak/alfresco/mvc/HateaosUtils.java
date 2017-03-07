package com.gradecak.alfresco.mvc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.PagedResources.PageMetadata;
import org.springframework.hateoas.Resource;
import org.springframework.util.Assert;

public class HateaosUtils
{

	public static <R> Resource<R> toResource(final R resource)
	{
		return new Resource<R>(resource);
	}

	public static <R> PagedResources<R> toResources(final List<R> resources, final PageMetadata pageMetadata)
	{
		return new PagedResources<R>(resources, pageMetadata);
	}

	public static <S> PagedResources<S> createPagedResource(final Page<S> page)
	{
		Assert.notNull(page, "Page must not be null!");

		final List<S> resources = new ArrayList<S>(page.getNumberOfElements());
		resources.addAll(page.getContent());

		final PagedResources<S> pagedResources = new PagedResources<S>(resources, asPageMetadata(page));
		return pagedResources;
	}

	private static <T> PageMetadata asPageMetadata(final Page<T> page)
	{
		Assert.notNull(page, "Page must not be null!");
		return new PageMetadata(page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages());
	}
}
