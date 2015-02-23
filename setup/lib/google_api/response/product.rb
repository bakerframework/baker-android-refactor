module GoogleApi
  module Response
    
    class Product
      attr_reader :purchased_at, :purchase_time, :consumption_state, :developer_payload
  
      def initialize(data)
        @purchase_state = data["purchaseState"]
        @consumption_state = data["consumptionState"]
        @developer_payload = data["developerPayload"]
        @purchase_time = Time.at(data["purchaseTimeMillis"].to_i/1000).to_datetime
      end
      
      def active_from
        @purchase_time
      end
      
      def active_to
        nil
      end
      
      def to_hash
        Hash[instance_variables.map { |var| [var[1..-1].to_sym, instance_variable_get(var)] }]
      end
    end
    
  end
end
