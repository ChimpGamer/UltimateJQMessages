package nl.chimpgamer.ultimatejqmessages.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class UltimateJQMessagesLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        var dependencies = new ArrayList<String>();
        dependencies.add("org.jetbrains.kotlin:kotlin-stdlib:2.4.0");
        dependencies.add("org.jetbrains.exposed:exposed-core:1.3.0");
        dependencies.add("org.jetbrains.exposed:exposed-dao:1.3.0");
        dependencies.add("org.jetbrains.exposed:exposed-jdbc:1.3.0");
        dependencies.add("org.xerial:sqlite-jdbc:3.47.1.0");
        dependencies.add("org.mariadb.jdbc:mariadb-java-client:3.5.1");
        dependencies.add("org.incendo:cloud-core:2.1.0");
        dependencies.add("org.incendo:cloud-paper:2.0.0");
        dependencies.add("org.incendo:cloud-minecraft-extras:2.0.0");
        dependencies.add("org.incendo:cloud-kotlin-coroutines:2.1.0");
        dependencies.add("org.incendo:cloud-kotlin-extensions:2.1.0");
        dependencies.add("dev.dejvokep:boosted-yaml:1.3.7");
        dependencies.add("com.zaxxer:HikariCP:6.2.1");
        dependencies.add("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.7.0");

        var mavenLibraryResolver = new MavenLibraryResolver();
        dependencies.forEach(dependency -> mavenLibraryResolver.addDependency(new Dependency(new DefaultArtifact(dependency), null)));
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("networkmanager", "default", "https://repo.networkmanager.xyz/repository/maven-public/").build());

        classpathBuilder.addLibrary(mavenLibraryResolver);
    }
}
