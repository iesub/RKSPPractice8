cd ApiGateway
cmd /c RebuildApiGateway.bat
cd ../Authservice
cmd /c RebuildAuthService.bat
cd ../EurekaService
cmd /c RebuildEurekaService.bat
cd ../GameControllerService
cmd /c RebuildGameController.bat
cd ../GameEngineService
cmd /c RebuildGameEngine.bat

pause