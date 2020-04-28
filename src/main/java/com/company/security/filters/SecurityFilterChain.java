package com.company.security.filters;

import com.company.infrastructure.Injector;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SecurityFilterChain implements FilterChain {
    private final List<SecurityFilter> filterList;

    public SecurityFilterChain() {
        // load filters implementation, so whenever you define a new security filter it will be applied to all http requests
        this.filterList = Injector.getImplementations(SecurityFilter.class).stream()
                .map(obj -> (SecurityFilter) obj)
                .collect(Collectors.toList());
    }

    @Override
    public void executeFilters(HttpExchange httpExchange) throws IOException {
        for (SecurityFilter filter : filterList) {
            filter.execute(httpExchange);
        }
    }
}
