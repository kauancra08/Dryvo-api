package br.com.kauangoncalves.drywall_api.material;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.kauangoncalves.drywall_api.user.User;
import br.com.kauangoncalves.drywall_api.user.UserRepository;

@Service
public class MaterialService {
    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;
    public MaterialService(MaterialRepository materialRepository, UserRepository userRepository) {
        this.materialRepository = materialRepository; this.userRepository = userRepository;
    }
    @Transactional
    public Material create(Long userId, MaterialRequest request) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        Material material = new Material();
        material.setOwner(owner); apply(material, request);
        return materialRepository.save(material);
    }
    @Transactional
    public Material update(Long userId, Long id, MaterialRequest request) {
        Material material = findOwned(userId,id); apply(material,request); return materialRepository.save(material);
    }
    @Transactional
    public void delete(Long userId, Long id) { materialRepository.delete(findOwned(userId,id)); }
    @Transactional(readOnly=true)
    public List<Material> findAll(Long userId) { return materialRepository.findAllByOwnerIdOrderByNameAsc(userId); }
    private void apply(Material m, MaterialRequest r) {
        m.setName(r.name().trim()); m.setCategory(normalize(r.category())); m.setUnit(MaterialUnit.valueOf(r.unit()));
        m.setUnitPrice(r.unitPrice()); m.setSupplier(normalize(r.supplier())); m.setNotes(normalize(r.notes()));
    }
    private Material findOwned(Long userId, Long id) {
        Material m=materialRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Material não encontrado"));
        if(!m.getOwner().getId().equals(userId)) throw new IllegalArgumentException("Material não pertence ao usuário"); return m;
    }
    private String normalize(String v){ if(v==null)return null; String t=v.trim(); return t.isEmpty()?null:t; }
}
