/*
 * Copyright (C) 2023, Ashley Scopes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ascopes.protobufmavenplugin;

import io.github.ascopes.protobufmavenplugin.generate.ImmutableGenerationRequest;
import io.github.ascopes.protobufmavenplugin.generate.SourceCodeGenerator;
import io.github.ascopes.protobufmavenplugin.generate.SourceRootRegistrar;
import io.github.ascopes.protobufmavenplugin.dependency.PluginBean;
import java.nio.file.Path;
import java.util.Set;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.jspecify.annotations.Nullable;

/**
 * Abstract base for a code-generation MOJO.
 *
 * @author Ashley Scopes
 */
public abstract class AbstractGenerateMojo extends AbstractMojo {

  /**
   * The source code generator.
   */
  @Component
  private SourceCodeGenerator sourceCodeGenerator;

  /**
   * The active Maven session.
   */
  @Parameter(required = true, readonly = true, property = "maven.session")
  private MavenSession mavenSession;

  /**
   * The version of protoc to use.
   *
   * <p>This should correspond to the version of {@code protobuf-java} or similar that is in
   * use.
   *
   * <p>The value can be a static version, or a valid Maven version range (such as
   * "{@code [3.5.0,4.0.0)}"). It is recommended to use a static version to ensure your builds are
   * reproducible.
   *
   * <p>If set to "{@code PATH}", then {@code protoc} is resolved from the system path rather than
   * being downloaded. This is useful if you need to use an unsupported architecture/OS, or a
   * development version of {@code protoc}.
   *
   * @since 0.0.1
   */
  @Parameter(required = true, property = "protoc.version")
  private String protocVersion;

  /**
   * Override the source directories to compile from.
   *
   * <p>Leave unspecified or explicitly null/empty to use the defaults.
   *
   * @since 0.0.1
   */
  @Parameter
  private @Nullable Set<String> sourceDirectories;

  /**
   * Specify additional paths to import protobuf sources from on the local file system.
   *
   * <p>These will not be compiled into Java sources directly.
   *
   * <p>If you wish to depend on a JAR containing protobuf sources, add it as a dependency
   * with the {@code provided} scope instead.
   *
   * @since 0.1.0
   */
  @Parameter
  private @Nullable Set<String> additionalImportPaths;

  /**
   * Additional plugins to use with the protobuf compiler.
   *
   * <p>Each plugin must have an {@code id}, which is used as the flag to pass to {@code protobuf}
   * (e.g. {@code --reactor_out=path} would have an ID of "{@code reactor}").
   *
   * <p>Plugins must be specified with at least one of:
   *
   * <ul>
   *   <li>A {@code dependency} block that points to a Maven artifact.</li>
   *   <li>An {@code executableName} block that refers to an executable on the system path.</li>
   * </ul>
   *
   * <p>If dependency blocks omit the {@code type} attribute, then they will default to
   * "{@code exe}", likewise if a {@code classifier} attribute is omitted, then it will use a value
   * appropriate to the operating system and architecture.
   *
   * @since 0.1.0
   */
  @Parameter
  private @Nullable Set<PluginBean> additionalPlugins;

  /**
   * Override the directory to output generated code to.
   *
   * <p>Leave unspecified or explicitly null to use the defaults.
   *
   * @since 0.1.0
   */
  @Parameter
  private @Nullable String outputDirectory;

  /**
   * Whether to treat {@code protoc} compiler warnings as errors.
   *
   * @since 0.0.1
   */
  @Parameter(defaultValue = "false")
  private boolean fatalWarnings;

  /**
   * Whether to also generate Kotlin API wrapper code around the generated Java code.
   *
   * @since 0.1.0
   */
  @Parameter(defaultValue = "false")
  private boolean kotlinEnabled;

  /**
   * Whether to only generate "lite" messages or not.
   *
   * <p>These are bare-bones sources that do not contain most of the metadata that regular
   * Protobuf sources contain, and are designed for low-latency/low-overhead scenarios.
   *
   * <p>See the protobuf documentation for the pros and cons of this.
   *
   * @since 0.0.1
   */
  @Parameter(defaultValue = "false")
  private boolean liteOnly;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    var actualOutputDirectory = outputDirectory == null || outputDirectory.isBlank()
        ? defaultOutputDirectory(mavenSession)
        : Path.of(outputDirectory);

    var request = ImmutableGenerationRequest.builder()
        .isKotlinEnabled(kotlinEnabled)
        .isLiteEnabled(liteOnly)
        .outputDirectory(actualOutputDirectory)
        .sourceRootRegistrar(sourceRootRegistrar())
        .build();

    sourceCodeGenerator.generate(request);
  }

  protected abstract SourceRootRegistrar sourceRootRegistrar();

  protected abstract Path defaultOutputDirectory(MavenSession session);
}
