module GoogleApi
  module Response
    
    class Subscription
      attr_reader :start_time, :expiry_time, :auto_renewing
  
      def initialize(data)
        @start_time = Time.at(data["startTimeMillis"].to_i/1000).to_datetime
        @expiry_time = Time.at(data["expiryTimeMillis"].to_i/1000).to_datetime
        @auto_renewing = data["autoRenewing"]
      end
      
      def active_from
        @start_time
      end
      
      def active_to
        @expiry_time
      end
      
      def to_hash
        Hash[instance_variables.map { |var| [var[1..-1].to_sym, instance_variable_get(var)] }]
      end
    end
    
  end
end