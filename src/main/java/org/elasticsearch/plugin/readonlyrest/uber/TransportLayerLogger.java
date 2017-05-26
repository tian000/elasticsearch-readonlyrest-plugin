/*
 *    This file is part of ReadonlyREST.
 *
 *    ReadonlyREST is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    ReadonlyREST is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with ReadonlyREST.  If not, see http://www.gnu.org/licenses/
 */

package org.elasticsearch.plugin.readonlyrest.uber;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.plugin.readonlyrest.ESContext;
import org.elasticsearch.plugin.readonlyrest.requestcontext.RequestContext;

import java.io.IOException;

/**
 * Created by napas on 5/25/17.
 */
public class TransportLayerLogger {

  private static final String SUCCESS_STRING = "SUCCESS";
  private static final String FAILURE_STRING = "FAILURE";
  private final Logger logger;

  public TransportLayerLogger(ESContext esContext) {
    logger = esContext.logger(getClass());
  }

  public void onResponse(ActionRequest request, RequestContext requestContext, ActionResponse response) throws IOException {
    if (response instanceof SearchResponse && request instanceof SearchRequest) {
      SearchRequest searchRequest = (SearchRequest) request;
      SearchResponse searchResponse = (SearchResponse) response;

      XContentBuilder b = JsonXContent.contentBuilder();
      b.startObject();
      b.field("logMessage", SUCCESS_STRING);
      b.field("totalShards", searchResponse.getTotalShards());
      b.field("successfulShards", searchResponse.getSuccessfulShards());
      b.field("hits", searchResponse.getHits().getTotalHits());
      b.array("indices", searchRequest.indices());
      b.field("searchType", searchRequest.searchType().name());
      b.field("action", requestContext.getAction());
      b.field("clusterName", requestContext.getClusterName());
      b.field("took", searchResponse.getTook());
      b.endObject();

      logger.info(b.string());
    }

  }

  public void onFailure(ActionRequest request, RequestContext requestContext, Exception e) throws IOException {
    if (request instanceof SearchRequest) {
      SearchRequest searchRequest = (SearchRequest) request;

      XContentBuilder b = JsonXContent.contentBuilder();
      b.startObject();
      b.field("logMessage", FAILURE_STRING);
      b.array("indices", searchRequest.indices());
      b.field("searchType", searchRequest.searchType().name());
      b.field("action", requestContext.getAction());
      b.field("clusterName", requestContext.getClusterName());
      b.field("errorMessage", e.getMessage());
      b.endObject();

      logger.info(b.string());
    }
  }
}