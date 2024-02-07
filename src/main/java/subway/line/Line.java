package subway.line;

import subway.exception.SubwayException;
import subway.section.Section;
import subway.station.Station;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Line {

    @OneToMany(mappedBy = "line", cascade = CascadeType.PERSIST, orphanRemoval = true)
    List<Section> sections = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 20, nullable = false)
    private String name;
    @Column(length = 20, nullable = false)
    private String color;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "up_station_id")
    private Station upStation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "down_station_id")
    private Station downStation;

    public Line() {
    }

    public Line(String name, String color, Station upStation, Station downStation, Long distance) {
        this.name = name;
        this.color = color;
        this.upStation = upStation;
        this.downStation = downStation;
        addSection(upStation, downStation, distance);
    }

    public void update(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public void updateDownStation(Station downStation) {
        this.downStation = downStation;
    }

    public void addSection(Station upStation, Station downStation, Long distance) {
        Section section = new Section(this, upStation, downStation, distance);
        if (sections.size() > 0) {
            validateNextSection(section);
            validateDuplicateStation(section);
        }
        this.sections.add(section);
    }

    private void validateNextSection(Section section) {
        if (!this.downStation.isEquals(section.getUpStation())) {
            throw new SubwayException("구간의 상행역은 해당 노선에 등록되어있는 하행 종점역이 아닙니다.");
        }
    }

    private void validateDuplicateStation(Section section) {
        if (isContains(section.getDownStation())) {
            throw new SubwayException("이미 등록되어있는 역입니다.");
        }
    }

    private boolean isContains(Station station) {
        return this.sections.stream().anyMatch(section -> section.getUpStation().equals(station));
    }

    public void removeSection(Long stationId) {
        validateLastSection();
        validateEndSection(stationId);

        Section deleteSection = this.sections.stream()
                .filter(section -> section.getDownStation().isEquals(stationId))
                .findFirst()
                .orElseThrow(() -> new SubwayException("역을 찾을 수 없습니다."));

        this.sections.remove(deleteSection);
    }

    private void validateLastSection() {
        if (sections.size() < 2) {
            throw new SubwayException("구간이 1개인 경우 역을 삭제할 수 없습니다.");
        }
    }

    private void validateEndSection(Long stationId) {
        if (!this.downStation.isEquals(stationId)) {
            throw new SubwayException("마지막 구간만 제거할 수 있습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }

    public List<Section> getSections() {
        return sections;
    }

}
