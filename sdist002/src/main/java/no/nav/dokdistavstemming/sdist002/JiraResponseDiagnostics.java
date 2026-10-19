package no.nav.dokdistavstemming.sdist002;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.http.HttpHeaders.CONTENT_ENCODING;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;

@Slf4j
final class JiraResponseDiagnostics implements ClientHttpResponse {

	private static final int PREFIX_LENGTH = 16;

	private final ClientHttpResponse response;
	private final PushbackInputStream body;
	private boolean prefixLogged;

	JiraResponseDiagnostics(ClientHttpResponse response) throws IOException {
		this.response = response;
		this.body = new PushbackInputStream(response.getBody(), PREFIX_LENGTH);
	}

	@Override
	public HttpStatusCode getStatusCode() throws IOException {
		return response.getStatusCode();
	}

	@Override
	public String getStatusText() throws IOException {
		return response.getStatusText();
	}

	@Override
	public HttpHeaders getHeaders() {
		return response.getHeaders();
	}

	@Override
	public InputStream getBody() throws IOException {
		if (!prefixLogged) {
			byte[] prefix = body.readNBytes(PREFIX_LENGTH);
			body.unread(prefix);
			prefixLogged = true;
			String nulByteOffsets = IntStream.range(0, prefix.length)
					.filter(index -> prefix[index] == 0)
					.mapToObj(Integer::toString)
					.collect(Collectors.joining(",", "[", "]"));
			log.info("Jira Cloud responsdiagnostikk: status={}, contentType={}, contentEncoding={}, contentLength={}, prefixLength={}, encodingHint={}, nulByteOffsets={}, location={}",
					getStatusCode().value(), header(CONTENT_TYPE), header(CONTENT_ENCODING), header(CONTENT_LENGTH),
					prefix.length, encodingHint(prefix), nulByteOffsets, redirectLocation());
		}
		return body;
	}

	@Override
	public void close() {
		response.close();
	}

	private String header(String name) {
		return Objects.toString(getHeaders().getFirst(name), "<missing>").replace('\r', ' ').replace('\n', ' ');
	}

	private String redirectLocation() {
		String location = getHeaders().getFirst(LOCATION);
		if (location == null) {
			return "<missing>";
		}
		try {
			URI uri = new URI(location);
			if (uri.isOpaque()) {
				return "<opaque>";
			}
			return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), null, null)
					.toASCIIString();
		} catch (URISyntaxException e) {
			return "<invalid>";
		}
	}

	private static String encodingHint(byte[] prefix) {
		if (prefix.length >= 4 && prefix[0] == 0 && prefix[1] == 0
				&& prefix[2] == (byte) 0xfe && prefix[3] == (byte) 0xff) {
			return "UTF-32BE-BOM";
		}
		if (prefix.length >= 4 && prefix[0] == (byte) 0xff && prefix[1] == (byte) 0xfe
				&& prefix[2] == 0 && prefix[3] == 0) {
			return "UTF-32LE-BOM";
		}
		if (prefix.length >= 2) {
			if (prefix[0] == 0x1f && prefix[1] == (byte) 0x8b) {
				return "gzip";
			}
			if (prefix[0] == (byte) 0xfe && prefix[1] == (byte) 0xff) {
				return "UTF-16BE-BOM";
			}
			if (prefix[0] == (byte) 0xff && prefix[1] == (byte) 0xfe) {
				return "UTF-16LE-BOM";
			}
		}
		return "unrecognized";
	}
}
