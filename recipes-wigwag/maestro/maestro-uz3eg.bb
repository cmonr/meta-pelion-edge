require maestro_0.0.1.inc

COMPATIBLE_MACHINE = "uz3eg-iocc"

PROVIDES += " maestro "
RPROVIDES_${PN} += " maestro "

FILESEXTRAPATHS_prepend := "${THISDIR}/maestro/uz3eg:"
SRC_URI += "file://maestro-config-uz3eg.yaml \
            file://devicedb.template.conf \
            file://0001-aarch64-arm.patch \
            file://0002-eventfd2.patch \
            file://0003-build-with-autoconf.patch \
            "

do_install_append() {
    # Maestro configuration management
    install -d ${D}/${RUN_CONFIG_DIR}
    install -d ${D}/${TEMPLATE_CONFIG_DIR}
    install -m 0644 ${WORKDIR}/maestro-config-uz3eg.yaml ${D}/${RUN_CONFIG_DIR}/maestro-config.yaml
    install -m 0644 ${WORKDIR}/devicedb.template.conf ${D}/${TEMPLATE_CONFIG_DIR}/devicedb.template.conf
}
