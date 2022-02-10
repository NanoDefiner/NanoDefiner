# Releasing a new NanoDefiner version

## Bump version

- in `pom.xml`
- in `docker/Dockerfile`
- in `docker/docker-compose.yaml`

## Git

- push
- create tag: `git tag vX.X.X`
- push tag: `git push origin vX.X.X`

## GitHub

- create release WAR: `make`
- create upload archive: `./scripts/create_archive.sh`
- upload `/tmp/nanodefiner_archive/nanodefiner.zip` to GitHub
- rename from `nanodefiner.zip` to `nanodefiner_X.X.X.zip`

## Docker

- inside the `docker` subfolder (where the `Dockerfile` is located), run: `docker build -t nanodefiner/nanodefiner:X.X.X -f Dockerfile .`
- then, do the same for the `latest` tag: `docker build -t nanodefiner/nanodefiner:latest -f Dockerfile .`
- login to DockerHub with your account: `docker login -u your_username`
- push the new image to both `X.X.X` and `latest`: `docker push nanodefiner/nanodefiner:X.X.X` and `docker push nanodefiner/nanodefiner:latest`
- make sure to log out, otherwise all users who are in the `docker` group as well as all people with access to the root account on the machine can read your unencrypted DockerHub password: `docker logout`
- update README on the [DockerHub repository](https://hub.docker.com/repository/docker/nanodefiner/nanodefiner).
