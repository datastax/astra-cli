FROM registry.access.redhat.com/ubi9/ubi-minimal:9.2

WORKDIR /work/

RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work

COPY --chown=1001:root build/native/nativeCompile/astra /work/application

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application"]
CMD ["./application"]
