HOMEPAGE = "https://github.com/containernetworking/cni"
SUMMARY = "Container Network Interface - networking for Linux containers"
DESCRIPTION = "CNI (Container Network Interface), a Cloud Native Computing \
Foundation project, consists of a specification and libraries for writing \
plugins to configure network interfaces in Linux containers, along with a \
number of supported plugins. CNI concerns itself only with network connectivity \
of containers and removing allocated resources when the container is deleted. \
Because of this focus, CNI has a wide range of support and the specification \
is simple to implement. \
"

SRCREV_cni = "4cfb7b568922a3c79a23e438dc52fe537fc9687e"
# Version 0.8.5
SRCREV_plugins = "1f33fb729ae2b8900785f896df2dc1f6fe5e8239"
SRC_URI = "\
	git://github.com/containernetworking/cni.git;nobranch=1;name=cni \
        git://github.com/containernetworking/plugins.git;nobranch=1;destsuffix=${S}/src/github.com/containernetworking/plugins;name=plugins \
        file://c2d \
        file://c2d-inner \
	"

RPROVIDES_${PN} += "kubernetes-cni"
RDEPENDS_${PN} += "bash"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://src/import/LICENSE;md5=fa818a259cbed7ce8bc2a22d35a464fc"

GO_IMPORT = "import"

PV = "0.7.1+git${SRCREV_cni}"

inherit go
inherit goarch

do_compile() {
	mkdir -p ${S}/src/github.com/containernetworking
	ln -sfr ${S}/src/import ${S}/src/github.com/containernetworking/cni

	cd ${B}/src/github.com/containernetworking/cni/libcni
	${GO} build

	cd ${B}/src/github.com/containernetworking/cni/cnitool
	${GO} build

	cd ${B}/src/github.com/containernetworking/plugins
	PLUGINS="$(ls -d plugins/meta/*; ls -d plugins/ipam/*; ls -d plugins/main/* | grep -v windows)"
	mkdir -p ${B}/plugins/bin/
	for p in $PLUGINS; do
	    plugin="$(basename "$p")"
	    echo "building: $p"
	    ${GO} build -mod=vendor -o ${B}/plugins/bin/$plugin github.com/containernetworking/plugins/$p
	done
}

do_install() {
    localbindir="/wigwag/system/opt/cni/bin"

    install -d ${D}${localbindir}
    install -d ${D}/${sysconfdir}/cni/net.d

    install -m 755 ${S}/src/import/cnitool/cnitool ${D}/${localbindir}
    install -m 755 -D ${B}/plugins/bin/* ${D}/${localbindir}

    # Parts of k8s expect the cni binaries to be available in /opt/cni
    install -d ${D}/opt/cni
    ln -sf /wigag/system/opt/cni/ ${D}/opt/cni/bin
    # re-linking to the origional recipeies location
    install -d ${D}${libexecdir}/cni
    ln -sf /wigwag/system/opt/cni ${D}${libexecdir}/cni
    #extra features needed by pe
    install -m 755 -o root -g root ${WORKDIR}/c2d ${D}/${localbindir}
    install -m 755 -o root -g root ${WORKDIR}/c2d-inner ${D}/${localbindir}
}

FILES_${PN} += "${libexecdir}cni/* /opt/cni/* /wigwag/system/opt/cni/bin"

INSANE_SKIP_${PN} += "ldflags already-stripped"

deltask compile_ptest_base
