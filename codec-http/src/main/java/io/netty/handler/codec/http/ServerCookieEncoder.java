package io.netty.handler.codec.http;

import static io.netty.handler.codec.http.CookieEncoderUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Encodes server-side {@link Cookie}s into HTTP header values.  This encoder can encode
 * the HTTP cookie version 0, 1, and 2.
 * <pre>
 * // Example
 * {@link HttpRequest} req = ...;
 * res.setHeader("Set-Cookie", {@link ServerCookieEncoder}.encode("JSESSIONID", "1234"));
 * </pre>
 *
 * @see CookieDecoder
 *
 * @apiviz.stereotype utility
 * @apiviz.has        io.netty.handler.codec.http.Cookie oneway - - encodes
 */
public final class ServerCookieEncoder {

    /**
     * Encodes the specified cookie into an HTTP header value.
     */
    public static String encode(String name, String value) {
        return encode(new DefaultCookie(name, value));
    }

    public static String encode(Cookie cookie) {
        if (cookie == null) {
            throw new NullPointerException("cookie");
        }

        StringBuilder buf = new StringBuilder();

        add(buf, cookie.getName(), cookie.getValue());

        if (cookie.getMaxAge() >= 0) {
            if (cookie.getVersion() == 0) {
                addUnquoted(buf, CookieHeaderNames.EXPIRES,
                        new HttpHeaderDateFormat().format(
                                new Date(System.currentTimeMillis() +
                                         cookie.getMaxAge() * 1000L)));
            } else {
                add(buf, CookieHeaderNames.MAX_AGE, cookie.getMaxAge());
            }
        }

        if (cookie.getPath() != null) {
            if (cookie.getVersion() > 0) {
                add(buf, CookieHeaderNames.PATH, cookie.getPath());
            } else {
                addUnquoted(buf, CookieHeaderNames.PATH, cookie.getPath());
            }
        }

        if (cookie.getDomain() != null) {
            if (cookie.getVersion() > 0) {
                add(buf, CookieHeaderNames.DOMAIN, cookie.getDomain());
            } else {
                addUnquoted(buf, CookieHeaderNames.DOMAIN, cookie.getDomain());
            }
        }
        if (cookie.isSecure()) {
            buf.append(CookieHeaderNames.SECURE);
            buf.append((char) HttpConstants.SEMICOLON);
        }
        if (cookie.isHttpOnly()) {
            buf.append(CookieHeaderNames.HTTPONLY);
            buf.append((char) HttpConstants.SEMICOLON);
        }
        if (cookie.getVersion() >= 1) {
            if (cookie.getComment() != null) {
                add(buf, CookieHeaderNames.COMMENT, cookie.getComment());
            }

            add(buf, CookieHeaderNames.VERSION, 1);

            if (cookie.getCommentUrl() != null) {
                addQuoted(buf, CookieHeaderNames.COMMENTURL, cookie.getCommentUrl());
            }

            if (!cookie.getPorts().isEmpty()) {
                buf.append(CookieHeaderNames.PORT);
                buf.append((char) HttpConstants.EQUALS);
                buf.append((char) HttpConstants.DOUBLE_QUOTE);
                for (int port: cookie.getPorts()) {
                    buf.append(port);
                    buf.append((char) HttpConstants.COMMA);
                }
                buf.setCharAt(buf.length() - 1, (char) HttpConstants.DOUBLE_QUOTE);
                buf.append((char) HttpConstants.SEMICOLON);
            }
            if (cookie.isDiscard()) {
                buf.append(CookieHeaderNames.DISCARD);
                buf.append((char) HttpConstants.SEMICOLON);
            }
        }

        return stripTrailingSeparator(buf);
    }

    public static List<String> encode(Cookie... cookies) {
        if (cookies == null) {
            throw new NullPointerException("cookies");
        }

        List<String> encoded = new ArrayList<String>(cookies.length);
        for (Cookie c: cookies) {
            if (c == null) {
                break;
            }
            encoded.add(encode(c));
        }
        return encoded;
    }

    public static List<String> encode(Collection<Cookie> cookies) {
        if (cookies == null) {
            throw new NullPointerException("cookies");
        }

        List<String> encoded = new ArrayList<String>(cookies.size());
        for (Cookie c: cookies) {
            if (c == null) {
                break;
            }
            encoded.add(encode(c));
        }
        return encoded;
    }

    public static List<String> encode(Iterable<Cookie> cookies) {
        if (cookies == null) {
            throw new NullPointerException("cookies");
        }

        List<String> encoded = new ArrayList<String>();
        for (Cookie c: cookies) {
            if (c == null) {
                break;
            }
            encoded.add(encode(c));
        }
        return encoded;
    }

    private ServerCookieEncoder() {
        // Unused
    }
}