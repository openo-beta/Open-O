//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.hospitalReportManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for {@link SFTPConnector#requirePrivateKeyDirectory(String)}.
 * <p>
 * The sFTP private key directory comes from the OMD_DIRECTORY property, or is derived from the
 * document directory when that property is unset. A misconfigured value is accepted by the upload
 * and rejected by the fetch, so it is validated in one place used by both.
 *
 * @since 2026-07-30
 */
@Tag("unit")
@Tag("hrm")
class SFTPConnectorPrivateKeyDirectoryUnitTest {

    @Test
    @DisplayName("should reject the filesystem root")
    void shouldReject_whenDirectoryIsFilesystemRoot() {
        assertThatThrownBy(() -> SFTPConnector.requirePrivateKeyDirectory("/"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("filesystem root")
                .hasMessageContaining("OMD_DIRECTORY");
    }

    @Test
    @DisplayName("should reject a path that resolves to the filesystem root")
    void shouldReject_whenDirectoryResolvesToFilesystemRoot() {
        assertThatThrownBy(() -> SFTPConnector.requirePrivateKeyDirectory("/tmp/.."))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("filesystem root");
    }

    @Test
    @DisplayName("should reject a relative directory")
    void shouldReject_whenDirectoryIsRelative() {
        assertThatThrownBy(() -> SFTPConnector.requirePrivateKeyDirectory("hrm/OMD"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("absolute path");
    }

    @Test
    @DisplayName("should reject an unset or blank directory")
    void shouldReject_whenDirectoryIsUnsetOrBlank() {
        assertThatThrownBy(() -> SFTPConnector.requirePrivateKeyDirectory(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not configured");
        assertThatThrownBy(() -> SFTPConnector.requirePrivateKeyDirectory("   "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    @DisplayName("should accept the default location derived from the document directory")
    void shouldAccept_whenDirectoryIsDerivedDefault() {
        String derived = "/var/lib/OscarDocument/oscar/document/../hrm/OMD/";

        assertThatCode(() -> SFTPConnector.requirePrivateKeyDirectory(derived))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should return the directory with surrounding whitespace removed")
    void shouldReturnTrimmedDirectory_whenDirectoryIsValid() {
        assertThat(SFTPConnector.requirePrivateKeyDirectory("  /var/lib/OscarDocument/oscar/hrm/OMD  "))
                .isEqualTo("/var/lib/OscarDocument/oscar/hrm/OMD");
    }
}
