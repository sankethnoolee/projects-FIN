package com.fintellix.dld.models;

import java.io.Serializable;

public class ClientUploaderDTO implements Serializable{

		private static final long serialVersionUID = 1L;
		
		private String clientCode;
		private String clientName;
		public String getClientCode() {
			return clientCode;
		}
		public void setClientCode(String clientCode) {
			this.clientCode = clientCode;
		}
		public String getClientName() {
			return clientName;
		}
		public void setClientName(String clientName) {
			this.clientName = clientName;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((clientCode == null) ? 0 : clientCode
							.hashCode());
			result = prime
					* result
					+ ((clientName == null) ? 0 : clientName
							.hashCode());
			
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ClientUploaderDTO other = (ClientUploaderDTO) obj;
			if (clientCode == null) {
				if (other.clientCode != null)
					return false;
			} else if (!clientCode.equals(other.clientCode))
				return false;
			
			if (clientName == null) {
				if (other.clientName != null)
					return false;
			} else if (!clientName.equals(other.clientName))
				return false;
			return true;
		}
		
}
