# toxic-typo

TEMP NOTES:
1. TT drawing
2. TT environment GitLab/Jenkins/JFrog
3. Read instructions and write down tasks

----------------------------------------------

TO DO PHASE A:
1. Understand ToxicTypo --> a web app in AWS allowing customer to use 'suggest-lib'; SaaS following CD principle; feature branches are the domain of devs; master branch is CD'ed to AWS
2. Run ToxicTypoApp in Docker container (maven with jdk)
3. Create a MBP:
	a. Feature branches --> CI + testing
	b. Master branch --> CI + testing + deploy to AWS (PROD)

HELPING CHECKPOINTS:
1. Download ToxicTypo to local machine
2. Push source code to a GitLab repo
3. Use maven tool to build the app (either in docker or in jenkins)
4. Use java image for runtime --> after mvn verify, you can find the ".jar" file in target folder >> The app should run on port 8080
5. Use Python 2.7 to run E2E tests --> '/src/test/e2e_tests.py'
6. Create a MBP:
	a. Feature branches --> CI + testing --> Build, Test, Report on Failure
	b. Master branch --> CI (Build) + testing + deploy to AWS (PROD)