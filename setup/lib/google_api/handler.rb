module GoogleApi
  class Handler
    attr_accessor :client, :access_token, :p12, :issuer, :email, :private_key, :public_key, :publisher, :bootstrapped, :retries

    def self.verify_local!(public_key, receipt)
      verified = public_key.verify( OpenSSL::Digest::SHA1.new, Base64.decode64(receipt.signature), receipt.data)
      raise InvalidSignatureError, "Invalid Signature" if not verified
      raise InvalidUserError, "Invalid User" if receipt.payload != receipt.user_id
    end

    # constructor
    def initialize(public_key, p12_binary)
      self.p12 = OpenSSL::PKCS12.new(p12_binary, "notasecret")
      self.issuer = p12.certificate.issuer.to_a[0][1]
      self.email = "#{issuer.split(".").first()}@developer.gserviceaccount.com"
      self.client = Google::APIClient.new(application_name: 'MagLoft', application_version: '1.0.0')
      self.private_key = p12.key
      self.public_key = OpenSSL::PKey::RSA.new(Base64.decode64(public_key))
      self.retries = 0
      self.client.authorization = Signet::OAuth2::Client.new(
        :token_credential_uri => 'https://accounts.google.com/o/oauth2/token',
        :audience => 'https://accounts.google.com/o/oauth2/token',
        :scope => 'https://www.googleapis.com/auth/androidpublisher',
        :issuer => email,
        :signing_key => self.private_key
      )
      authorize_token()
      self.publisher = self.client.discovered_api('androidpublisher', 'v2')
      self.bootstrapped = true
    end

    # public methods
    def verify(receipt)
      self.class.verify_local!(self.public_key, receipt)
      if receipt.purchase_type == "product"
        get_product(receipt)
      else
        get_subscription(receipt)
      end
    end

    # internal logic

    def get_product(receipt)
      Response::Product.new execute(self.publisher.purchases.products.get, { packageName: receipt.package_name, productId: receipt.sku, token: receipt.token })
    end
    
    def get_subscription(receipt)
      Response::Subscription.new execute(self.publisher.purchases.subscriptions.get, { packageName: receipt.package_name, subscriptionId: receipt.sku, token: receipt.token })
    end
  
    def authorize_token
      if self.access_token
        self.client.authorization.update_token!(self.access_token)
      else
        self.access_token = self.client.authorization.fetch_access_token!
      end
    end
  
    def execute(api_method, parameters, body_object=nil)
      request = Google::APIClient::Request.new({api_method: api_method, parameters: parameters, body_object: body_object})
      execute_request(request)
    end
    
    def execute_request(request)
      result = self.client.execute(request)
      case result.status
      when 200
        self.retries = 0
        JSON.parse(result.body)
      when 401
        if self.retries < 3
          self.retries = self.retries + 1
          authorize_token()
          execute(api_method, parameters)
        else
          raise BadRequestError, "ERROR##{result.status}: #{result.data.error["message"]}"
        end
      else
        binding.pry
        raise BadRequestError, "ERROR##{result.status}: #{result.data.error["message"]}"
      end
    end
    
    def play_edit(package_name, &block)
    
      # create edit
      edit = execute(publisher.edits.insert, {packageName: package_name})
    
      # run block
      block.call(edit["id"])
    
      # commit listing
      execute(publisher.edits.commit, {packageName: package_name, editId: edit["id"]})
      
    end
    
    class BadRequestError < StandardError
      def status_code
        400
      end
    end
    
    class InvalidSignatureError < StandardError
      def status_code
        400
      end
    end
    
    class InvalidUserError < StandardError
      def status_code
        404
      end
    end
    
  end
end


