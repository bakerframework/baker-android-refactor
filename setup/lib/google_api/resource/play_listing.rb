module GoogleApi
  module Resource
    
    class PlayListing
      attr_accessor :package_name, :language
      attr_accessor :title, :full_description, :short_description, :video
  
      def initialize(api, package_name, language="en-US")
        @api = api
        self.package_name = package_name
        self.language = language
      end
      
      def load(edit_id)
        listing = @api.execute(@api.publisher.edits.listings.get, { packageName: self.package_name, editId: edit_id, language: self.language })
        self.title = listing["title"]
        self.full_description = listing["fullDescription"]
        self.short_description = listing["shortDescription"]
        self.video = listing["video"]
      end
      
      def save(edit_id)
        
        # create edit
        edit = @api.execute(@api.publisher.edits.insert, {packageName: package_name})
        
        # update listing
        @api.execute(@api.publisher.edits.listings.patch, {packageName: self.package_name, editId: edit_id, language: self.language }, self.body)
      end
      
      def body
        {
          title: self.title,
          fullDescription: self.full_description,
          shortDescription: self.short_description,
          video: self.video
        }
      end
      
    end
    
  end
end
