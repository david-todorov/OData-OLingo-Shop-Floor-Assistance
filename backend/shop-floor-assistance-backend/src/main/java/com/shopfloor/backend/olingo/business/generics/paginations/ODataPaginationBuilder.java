package com.shopfloor.backend.olingo.business.generics.paginations;

import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * A builder class for creating pagination configurations for OData queries.
 * Implements the Pageable interface to provide pagination details.
 * Represent $skip and $top options in OData.
 * @author David Todorov (https://github.com/david-todorov)
 */
public class ODataPaginationBuilder implements Pageable {


    private int limit;
    private int offset;

    private final int DEFAULT_LIMIT = 100;
    private final int DEFAULT_OFFSET = 0;

    public ODataPaginationBuilder() {
        // Initialize with default values
        this.limit = DEFAULT_LIMIT;
        this.offset = DEFAULT_OFFSET;
    }

    /**
     * Adds the $top option to the pagination configuration.
     *
     * @param topOption the $top option specifying the maximum number of records to return
     * @return the ODataPaginationBuilder instance for method chaining
     * @throws IllegalArgumentException if the top value is less than or equal to 0
     */
    public ODataPaginationBuilder addTopOption(TopOption topOption) {
        if (topOption != null) {
            if (topOption.getValue() <= 0) {
                throw new IllegalArgumentException("Top value must be greater than 0.");
            }
            this.limit = topOption.getValue();
        }
        return this; // Return the builder itself for method chaining
    }

    /**
     * Adds the $skip option to the pagination configuration.
     *
     * @param skipOption the $skip option specifying the number of records to skip
     * @return the ODataPaginationBuilder instance for method chaining
     * @throws IllegalArgumentException if the skip value is less than 0
     */
    public ODataPaginationBuilder addSkipOption(SkipOption skipOption) {
        if (skipOption != null) {
            if (skipOption.getValue() < 0) {
                throw new IllegalArgumentException("Skip value must be greater than or equal to 0.");
            }
            this.offset = skipOption.getValue();
        }
        return this; // Return the builder itself for method chaining
    }

    /**
     * Builds the Pageable object with the current pagination configuration.
     *
     * @return the Pageable object representing the pagination configuration
     */
    public Pageable build() {
        return this; // Return the current instance as Pageable
    }

    @Override
    public int getPageNumber() {
        return 0; // Not using page numbers
    }

    @Override
    public int getPageSize() {
        return limit; // The page size is the limit, which is the number of records to return
    }

    @Override
    public long getOffset() {
        return offset; // The offset is the starting position for fetching records
    }

    @Override
    public Sort getSort() {
        return Sort.unsorted(); // Sorting handled elsewhere, so return unsorted
    }

    @Override
    public Pageable next() {
        throw new UnsupportedOperationException("Pagination beyond the first page is not supported.");
    }

    @Override
    public Pageable previousOrFirst() {
        return this; // No support for previous, so return the current instance
    }

    @Override
    public Pageable first() {
        return this; // Return the current instance (no page concept)
    }

    @Override
    public Pageable withPage(int pageNumber) {
        throw new UnsupportedOperationException("Direct page number navigation is not supported.");
    }

    @Override
    public boolean hasPrevious() {
        return false; // No previous pages, so return false
    }
}
