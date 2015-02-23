module GoogleApi
  module Response
    
    class Generic
      attr_reader :kind, :data
  
      def initialize(data)
        @kind = data["kind"]
        @data = data
      end
    end
    
  end
end
