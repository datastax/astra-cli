FROM registry.access.redhat.com/ubi9/ubi-minimal:9.2

WORKDIR /work

RUN chown 1001:root /work && chmod g+rwX /work

COPY --chown=1001:root build/native/nativeCompile/astra /work/cli

USER 1001

ENTRYPOINT ["./cli"]
