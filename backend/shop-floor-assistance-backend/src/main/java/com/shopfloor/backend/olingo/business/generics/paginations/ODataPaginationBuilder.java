package com.shopfloor.backend.olingo.business.generics.paginations;

import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    // Add TopOption for the limit
    public ODataPaginationBuilder addTopOption(TopOption topOption) {
        if (topOption != null) {
            if (topOption.getValue() <= 0) {
                throw new IllegalArgumentException("Top value must be greater than 0.");
            }
            this.limit = topOption.getValue();
        }
        return this; // Return the builder itself for method chaining
    }

    // Add SkipOption for the offset
    public ODataPaginationBuilder addSkipOption(SkipOption skipOption) {
        if (skipOption != null) {
            if (skipOption.getValue() < 0) {
                throw new IllegalArgumentException("Skip value must be greater than or equal to 0.");
            }
            this.offset = skipOption.getValue();
        }
        return this; // Return the builder itself for method chaining
    }

    // Build the Pageable object (finalized pagination configuration)
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
