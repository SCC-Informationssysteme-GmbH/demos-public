FROM debian:bookworm-slim
RUN apt-get update && apt-get install -y freecad --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /work
ENTRYPOINT ["freecadcmd"]
