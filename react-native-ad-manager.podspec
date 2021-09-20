require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-ad-manager"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-ad-manager
                   DESC
  s.homepage     = "https://github.com/NZME/react-native-ad-manager"
  s.license      = "MIT"
  s.license      = package['license']
  s.authors      = { "Matej Drobnic" => "matej.dr@gmail.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/NZME/react-native-ad-manager.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
  s.dependency 'Google-Mobile-Ads-SDK', '7.69.0'
  s.dependency 'GoogleMobileAdsMediationFacebook'
end

