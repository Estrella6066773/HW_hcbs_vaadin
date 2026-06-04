package com.hcbs.service.catalog;

import com.hcbs.config.FilmPosterCatalog;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 判断海报是否存在
 * Checks that a DB poster path points at a file served from {@code META-INF/resources}.
 */
@Service
public class PosterResourceService {

    /** Reject tiny placeholder files that render as blank/black in the browser. */
    private static final long MIN_POSTER_BYTES = 512;

    public boolean isAvailable(String posterUrl) {
        if (posterUrl == null || posterUrl.isBlank()) {
            return false;
        }
        String path = posterUrl.trim();
        if (!path.startsWith("/")) {
            return false;
        }
        ClassPathResource resource = new ClassPathResource("META-INF/resources" + path);
        if (!resource.exists()) {
            return false;
        }
        try {
            return resource.contentLength() >= MIN_POSTER_BYTES;
        } catch (IOException ex) {
            return false;
        }
    }

    public void requireLocalPath(String posterUrl) {
        if (posterUrl == null || posterUrl.isBlank()) {
            throw new IllegalArgumentException("Poster path is required");
        }
        String path = posterUrl.trim();
        if (!FilmPosterCatalog.isLocalPath(path)) {
            throw new IllegalArgumentException("Poster path must start with " + FilmPosterCatalog.BASE);
        }
    }
}
