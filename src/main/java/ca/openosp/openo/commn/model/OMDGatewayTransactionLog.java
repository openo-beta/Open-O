/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License. This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 * <p>
 * This software was written for the Department of Family Medicine McMaster University Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.commn.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
public class OMDGatewayTransactionLog extends AbstractModel<Integer> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  @Temporal(TemporalType.TIMESTAMP)
  private Date started;
  @Temporal(TemporalType.TIMESTAMP)
  private Date ended;
  private String initiatingProviderNo;
  private String transactionType;
  private String externalSystem;
  private Integer demographicNo;
  private Integer resultCode;
  private Boolean success;
  private String error;
  private String dataSent;
  private String dataRecieved;
  private String headers;
  private String uao;
  private String oscarSessionId;
  private String contextSessionId;
  private String uniqueSessionId;
  private String xRequestId;
  private String xLobTxId;
  private String xCorrelationId;
  private String xGtwyClientId;
  private Long secondsLeft;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Date getStarted() {
    return started;
  }

  public void setStarted(Date started) {
    this.started = started;
  }

  public String getInitiatingProviderNo() {
    return initiatingProviderNo;
  }

  public void setInitiatingProviderNo(String initiatingProviderNo) {
    this.initiatingProviderNo = initiatingProviderNo;
  }

  public String getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(String transactionType) {
    this.transactionType = transactionType;
  }

  public String getExternalSystem() {
    return externalSystem;
  }

  public void setExternalSystem(String externalSystem) {
    this.externalSystem = externalSystem;
  }

  public Integer getDemographicNo() {
    return demographicNo;
  }

  public void setDemographicNo(Integer demographicNo) {
    this.demographicNo = demographicNo;
  }

  public Integer getResultCode() {
    return resultCode;
  }

  public void setResultCode(Integer resultCode) {
    this.resultCode = resultCode;
  }

  public Boolean getSuccess() {
    return success;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getHeaders() {
    return headers;
  }

  public void setHeaders(String headers) {
    this.headers = headers;
  }

  public String getUao() {
    return uao;
  }

  public void setUao(String uao) {
    this.uao = uao;
  }

  public String getOscarSessionId() {
    return oscarSessionId;
  }

  public void setOscarSessionId(String oscarSessionId) {
    this.oscarSessionId = oscarSessionId;
  }

  public String getContextSessionId() {
    return contextSessionId;
  }

  public void setContextSessionId(String contextSessionId) {
    this.contextSessionId = contextSessionId;
  }

  public Long getSecondsLeft() {
    return secondsLeft;
  }

  public void setSecondsLeft(Long secondsLeft) {
    this.secondsLeft = secondsLeft;
  }

  public String getDataSent() {
    return dataSent;
  }

  public void setDataSent(String dataSent) {
    this.dataSent = dataSent;
  }

  public String getUniqueSessionId() {
    return uniqueSessionId;
  }

  public void setUniqueSessionId(String uniqueSessionId) {
    this.uniqueSessionId = uniqueSessionId;
  }

  public String getDataRecieved() {
    return dataRecieved;
  }

  public void setDataRecieved(String dataRecieved) {
    this.dataRecieved = dataRecieved;
  }

  public Date getEnded() {
    return ended;
  }

  public void setEnded(Date ended) {
    this.ended = ended;
  }

  public String getxRequestId() {
    return xRequestId;
  }

  public void setxRequestId(String xRequestId) {
    this.xRequestId = xRequestId;
  }

  public String getxLobTxId() {
    return xLobTxId;
  }

  public void setxLobTxId(String xLobTxId) {
    this.xLobTxId = xLobTxId;
  }

  public String getxCorrelationId() {
    return xCorrelationId;
  }

  public void setxCorrelationId(String xCorrelationId) {
    this.xCorrelationId = xCorrelationId;
  }

  public String getxGtwyClientId() {
    return xGtwyClientId;
  }

  public void setxGtwyClientId(String xGtwyClientId) {
    this.xGtwyClientId = xGtwyClientId;
  }
}