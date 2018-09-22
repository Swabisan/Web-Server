
package request;

import java.net.Socket;
import java.io.IOException;

public class RequestParser {

  private Request request;
  private Socket client;

  private BufferedReader bufferReader;
  private String currentLine = null;
  private int lineNo;

  private boolean badRequest = false;

  public RequestParser(Socket client, Request request) {
    this.client = client;
    this.request = request;
  }

  protected void parseHttpRequest() throws IOException {
    lineNo = 0;

    bufferReader = new BufferedReader(
      new InputStreamReader(client.getInputStream())
    );

    while((currentLine = bufferReader.readLine()) != null) {
      lineNo++;

      if (lineNo == 1) {
        if (!parseFirstLine(currentLine)) {
          break;
        }
      }

      if (lineNo > 1) {
        if (!parseHeaders(currentLine)) {
          if (!parseBody()) {
            flagBadRequest();
          }
          break;
        }
      }
    }
  }

  private boolean parseFirstLine(String firstLine) {
    String[] tokens = firstLine.split(" ");

    if (tokens.length < 3) {
      flagBadRequest();
      return false;
    }
    request.setMethod    (tokens[0]);
    request.setIdentifier(tokens[1]);
    request.setVersion   (tokens[2]);
    return true;
  }

  private boolean parseHeaders(String header) {
    String[] tokens = header.split(": ");

    if (noMoreHeaders(header)) {
      return false;
    }
    if (tokens.length < 2) {
      flagBadRequest();
      return false;
    }

    request.putHeader(tokens[0], tokens[1]);
    return true;
  }

  private boolean noMoreHeaders(String header) {
    return header.isEmpty();
  }

  private boolean parseBody() throws IOException {
    int contentLength = getContentLength();

    if (contentLength == -1) {
      return true;
    }

    char cBuff[] = new char[contentLength];
    bufferReader.read(cBuff, 0, contentLength);
    request.setBody(new String(cBuff));

    return true;
  }

  private int getContentLength() {
    String contentLength = request.getHeader("content-length");

    if (contentLength != "KEY_NOT_FOUND") {
      return Integer.parseInt(contentLength);
    }

    return -1;
  }

  private void flagBadRequest() {
    badRequest = true;
    System.out.println("400 Bad request");
  }

}