package org.ybigta.highlighter.stream.dto.response;

import java.util.List;
import java.util.Map;

public record ListStreamsResponse(
        List<Map<String, Boolean>> status
) {
}
