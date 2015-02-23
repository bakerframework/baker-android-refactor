require 'google_api/response/generic'
require 'google_api/response/product'
require 'google_api/response/subscription'

module GoogleApi
  module Response
    
    def self.from_data(data)
      case data["kind"]
      when "androidpublisher#subscriptionPurchase"
        SubscriptionPurchase.new(data)
      when "androidpublisher#productPurchase"
        ProductPurchase.new(data)
      else
        Generic.new(data)
      end
    end
      
    
  end
end
